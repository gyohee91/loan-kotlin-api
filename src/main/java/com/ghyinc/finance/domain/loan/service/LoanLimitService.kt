package com.ghyinc.finance.domain.loan.service

import com.ghyinc.finance.domain.loan.dto.ExternalDataContext.Companion.empty
import com.ghyinc.finance.domain.loan.dto.LoanLimitInquiryResponse
import com.ghyinc.finance.domain.loan.dto.LoanLimitInquiryResponse.Companion.from
import com.ghyinc.finance.domain.loan.dto.LoanLimitPollingResponse
import com.ghyinc.finance.domain.loan.dto.LoanLimitRequest
import com.ghyinc.finance.domain.loan.entity.LoanLimitInquiry.Companion.create
import com.ghyinc.finance.domain.loan.enums.InquiryStatus
import com.ghyinc.finance.domain.loan.factory.LoanLimitStrategyFactory
import com.ghyinc.finance.domain.loan.repository.LoanLimitInquiryRepository
import com.ghyinc.finance.domain.loan.repository.LoanLimitProductResultRepository
import com.ghyinc.finance.global.common.LoReqtNoGenerator
import com.ghyinc.finance.global.event.LoanLimitInquiryCreatedEvent
import org.apache.kafka.common.errors.InvalidRequestException
import org.slf4j.LoggerFactory
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 파트너 Entity
 *
 *
 * PartnerCode(Enum)는 코드 레벨 식별자로 Adaptor Factory 키 역할 담당
 * Partner(Entity)는 운영 중 변경이 필요한 메타데이터를 DB로 관리
 *
 * <pre>
 * PartnerCode(Enum): Adaptor 분기, 컴파일 타임 타입 안정성
 * Partner(Entity): 노출명, 활성화여부 등
</pre> *
 */
@Service
class LoanLimitService(
    private val loanLimitInquiryRepository: LoanLimitInquiryRepository,
    private val loanLimitProductResultRepository: LoanLimitProductResultRepository,
    private val strategyFactory: LoanLimitStrategyFactory,
    private val generator: LoReqtNoGenerator,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    private val log = LoggerFactory.getLogger(LoanLimitService::class.java)

    @Transactional
    fun requestCompareLoan(request: LoanLimitRequest): LoanLimitInquiryResponse {
        // 진행 중인 조회가 있으면 중복 요청 방지(당일 동일 유형 재조회 제한)
        val hasInProgress = loanLimitInquiryRepository.existsByUserIdAndLoanTypeAndStatus(
            request.userId,
            request.loanType,
            InquiryStatus.IN_PROGRESS
        )
        if (hasInProgress) {
            throw InvalidRequestException("진행 중인 한도조회가 있습니다.")
        }

        val strategy = strategyFactory.getStrategy(request.loanType)
        // 유효성 검증 (각 상품 type 별)
        strategy.validate(request)

        // External 데이터 조회 - Strategy가 알아서 처리
        val context = if (strategy.requiresExternalData())
            strategy.fetchExternalData(request)
        else
            empty()

        // Strategy: 대출 유형상 가능한 금융사(코드 레벨 고정)
        // DB      : 현재 활성화된 은행 (운영 팀이 배포 없이 제어)
        val activePartnerCodes = strategy.supportedBanks
        if (activePartnerCodes.isEmpty()) throw InvalidRequestException("현재 조회 가능한 금융사가 없습니다")

        // 외부 데이터 실패 시 진행 가능한 금융사만 필터링
        val availablePartnerCodes = strategy.filterAvailablePartners(activePartnerCodes, context)
        if (availablePartnerCodes.isEmpty()) {
            throw InvalidRequestException(
                "현재 조회 가능한 금융사가 없습니다. " +
                        context.errors.values.joinToString(", ") { it.message ?: "" }
            )
        }

        // LoanLimitInquiry INSERT
        val inquiry = create(
            generator.generate("LL"),
            request.userId,
            request.name,
            request.ci,
            request.loanType,
            request.jobType,
            request.jobName,
            request.joinDate,
            request.carNo,
            request.agreePersonalCreditInfo,
            request.agreePersonalCreditTime
        )

        loanLimitInquiryRepository.save(inquiry)

        // 어댑터 요청 DTO 변환 (Strategy)
        // 대출 유형별 전략으로 금융사 전송용 요청 DTO 생성
        val adaptorRequest = strategy.toAdaptorRequest(request, context)

        // 한도 조회(백그라운드 비동기 처리)
        // @Async 적용을 위해 별도 Bean(LoanLimitSenderService)으로 분리
        //loanLimitSenderService.inquiry(inquiry.getId(), activePartnerCodes, adaptorRequest);
        applicationEventPublisher.publishEvent(
            LoanLimitInquiryCreatedEvent.create(inquiry.id!!, activePartnerCodes, adaptorRequest)
        )

        return from(inquiry)
    }

    @Transactional(readOnly = true)
    fun getInquiryResult(inquiryNo: String, pageable: Pageable): LoanLimitPollingResponse {
        val inquiry = loanLimitInquiryRepository.findByInquiryNo(inquiryNo)
            .orElseThrow { InvalidRequestException("존재하지 않는 조회이력입니다: $inquiryNo") }

        val productResults = loanLimitProductResultRepository.findProductResultsByInquiryId(inquiry.id!!, pageable)

        return LoanLimitPollingResponse.from(inquiry, productResults)
    }
}
