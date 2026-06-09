package com.ghyinc.finance.domain.loan.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.ghyinc.finance.domain.loan.adaptor.dto.LoanLimitAdaptorRequest
import com.ghyinc.finance.domain.loan.adaptor.dto.LoanLimitAdaptorResponse
import com.ghyinc.finance.domain.loan.adaptor.impl.LoanLimitAdaptor
import com.ghyinc.finance.domain.loan.dto.RequestProduct
import com.ghyinc.finance.domain.loan.entity.LoanLimitProductResult
import com.ghyinc.finance.domain.loan.entity.LoanLimitResult
import com.ghyinc.finance.domain.loan.enums.InquiryStatus
import com.ghyinc.finance.domain.loan.enums.PartnerCode
import com.ghyinc.finance.domain.loan.enums.PartnerInquiryStatus
import com.ghyinc.finance.domain.loan.factory.LoanLimitAdaptorFactory
import com.ghyinc.finance.domain.loan.repository.LoanLimitInquiryRepository
import com.ghyinc.finance.domain.loan.repository.ProductRepository
import com.ghyinc.finance.global.common.LoReqtNoGenerator
import com.ghyinc.finance.global.event.LoanLimitCompletedEvent
import com.ghyinc.finance.global.event.LoanLimitInquiryCreatedEvent
import com.ghyinc.finance.global.outbox.entity.OutboxEvent.Companion.create
import com.ghyinc.finance.global.outbox.entity.OutboxStatus
import com.ghyinc.finance.global.outbox.event.OutboxCreatedEvent
import com.ghyinc.finance.global.outbox.repository.OutboxEventRepository
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import org.apache.kafka.common.errors.InvalidRequestException
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.RejectedExecutionException
import java.util.concurrent.TimeUnit

@Service
class LoanLimitSenderService(
    private val adaptorFactory: LoanLimitAdaptorFactory,
    private val loanLimitInquiryRepository: LoanLimitInquiryRepository,
    private val productRepository: ProductRepository,
    private val outboxEventRepository: OutboxEventRepository,

    private val generator: LoReqtNoGenerator,
    private val partnerApiExecutor: Executor,
    private val applicationEventPublisher: ApplicationEventPublisher,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(LoanLimitSenderService::class.java)

    companion object {
        private const val REQUEST_ID_KEY = "requestId"
    }

    /**
     * 여러 금융사에 대한 한도조회
     *
     *
     *  각 은행 API 호출은 독립적이므로 CompletableFuture로 병렬 처리
     * 한 금융사의 실패가 다른 금융사 조회에 영향을 주지 않음.
     * 전용 스레드 풀을 사용하여 외부 I/O가 공통 스레드 풀을 점유하지 않도록 격리
     */
    @Transactional
    fun inquiry(
        id: Long,
        partnerCodes: List<PartnerCode>,
        adaptorRequest: LoanLimitAdaptorRequest
    ) {
        // 새 트랜잭션에서 inquiry 조회 (호출 측 트랜잭션과 완전 분리)
        val loanLimitInquiry = loanLimitInquiryRepository.findById(id)
            .orElseThrow{ InvalidRequestException("존재하지 않는 조회 이력: $id") }

        loanLimitInquiry.updateInquiryStatus(InquiryStatus.IN_PROGRESS)

        try {
            // 각 금융사에 대한 Result 선저장
            val resultMap = partnerCodes.associateWith { partnerCode ->
                LoanLimitResult.create(
                    loanLimitInquiry = loanLimitInquiry,
                    partnerCode = partnerCode,
                    status = InquiryStatus.PENDING
                ).also { loanLimitInquiry.addResult(it) }
            }

            // 금융사별 상품 조회 및 ProductResult 선저장
            val productResultMap = partnerCodes.associateWith { partnerCode ->
                productRepository.findActiveByPartnerCodeAndLoanType(partnerCode, adaptorRequest.loanType)
                    .map { product ->
                        LoanLimitProductResult.create(
                            loanLimitInquiry = loanLimitInquiry,
                            loReqtNo = generator.generate("LR"),  //신청번호 채번
                            partnerCode = partnerCode,
                            productCode = product.productCode,
                            status = PartnerInquiryStatus.PENDING
                        ).also { loanLimitInquiry.addProductResult(it) }
                    }
            }

            // 상품 전체 수 초기화
            val totalProductCount = productResultMap.values.sumOf { it.size }
            loanLimitInquiry.initProductCount(totalProductCount)

            // 금융사별 RequestProduct(공통 요청 DTO) 구성
            val requestProductMap = productResultMap.mapValues { (_, results) ->
                results.map { productResult ->
                    RequestProduct(
                        loReqtNo = productResult.loReqtNo,
                        productCode = productResult.productCode
                    )
                }
            }


            // 금융사별 병렬 API 호출
            // partnerCode별 requestProducts 구성 후 병렬 호출
            val futures = partnerCodes.map { partnerCode ->
                //requestProducts를 포함한 요청 DTO 재구성
                val adaptorRequests = adaptorRequest.withRequestProducts(
                    requestProductMap[partnerCode] ?: emptyList()
                )
                val adaptor: LoanLimitAdaptor = adaptorFactory.getAdaptor(partnerCode)

                CompletableFuture
                    .supplyAsync({ adaptor.inquireLimit(partnerCode, adaptorRequests) }, partnerApiExecutor)
                    .orTimeout(8, TimeUnit.SECONDS)
                    .exceptionally { ex ->
                        when (ex.cause) {
                            // Circuit Breaker OPEN 시
                            is CallNotPermittedException -> {
                                log.warn("[{}] Circuit Breaker OPEN - 해당 금융사 격리", partnerCode, ex)
                                LoanLimitAdaptorResponse.fail(partnerCode, ex.message, 0L)
                            }

                            is RejectedExecutionException -> {
                                log.error("[{}] partnerApiExecutor 큐 초과", partnerCode)
                                LoanLimitAdaptorResponse.fail(partnerCode, "THREAD_POOL_EXHAUSTED", 0L)
                            }

                            else -> {
                                log.error("[{}] 비동기 한도조회 중 에러 발생", partnerCode, ex)
                                LoanLimitAdaptorResponse.fail(partnerCode, ex.message, 0L)
                            }
                        }
                    }
            }

            val adaptorResponses = futures.map { it.join() }

            // 어댑터 응답을 후처리하고 Entity로 변환하여 저장
            adaptorResponses.forEach { adaptorResponse ->
                val result = resultMap[adaptorResponse.partnerCode]
                if (adaptorResponse.success) {
                    result?.success(adaptorResponse.resTimeMs)
                    productResultMap[adaptorResponse.partnerCode] ?.forEach { it.sendSuccess() }
                } else {
                    result?.fail(adaptorResponse.failReason, adaptorResponse.resTimeMs)
                    productResultMap[adaptorResponse.partnerCode] ?.forEach { it.sendFail() }
                }
            }

            // 최종 상태 결정
            val successCount = adaptorResponses.count { it.success }
            val resultStatus = when(successCount) {
                adaptorResponses.size -> InquiryStatus.SUCCESS
                0 -> InquiryStatus.FAILED
                else -> InquiryStatus.PARTIAL_SUCCESS
            }

            loanLimitInquiry.updateInquiryStatus(resultStatus)

            // 알림 발송 - notification 도메인을 직접 알지 못함
            if (InquiryStatus.FAILED != resultStatus) {
                // Outbox INSERT (비즈니스 트랜잭션과 원자적)
                val outboxEvent = create(
                    aggregateType = "LoanLimitInquiry",
                    aggregateId = loanLimitInquiry.inquiryNo,
                    eventType = "LOAN_LIMIT_COMPLETED",
                    payload = objectMapper.writeValueAsString(
                        LoanLimitCompletedEvent.create(
                            inquiryNo = loanLimitInquiry.inquiryNo,
                            userId = loanLimitInquiry.userId,
                            name = loanLimitInquiry.name,
                            status = loanLimitInquiry.status,
                            requestId = MDC.get(REQUEST_ID_KEY)
                        )
                    ),
                    status = OutboxStatus.PENDING
                )

                outboxEventRepository.save(outboxEvent)

                //kafkalLoanLimitEventPublisher.publishCompletedEvent(event);
                //springLoanLimitEventPublisher.publishCompletedEvent(event);

                // Spring 이벤트 발행 (트랜잭션 커밋 후 Kafka 발행 트리거)
                applicationEventPublisher.publishEvent(OutboxCreatedEvent(outboxEvent.id))
            }
        } catch (e: Exception) {
            log.error("한도조회 처리 중 오류. id={}", loanLimitInquiry.id, e)
            loanLimitInquiry.updateInquiryStatus(InquiryStatus.FAILED)
        }
    }
}
