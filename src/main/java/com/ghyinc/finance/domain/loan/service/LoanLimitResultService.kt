package com.ghyinc.finance.domain.loan.service

import com.fasterxml.jackson.databind.JsonNode
import com.ghyinc.finance.domain.loan.adaptor.callback.LoanLimitResultAdaptorFactory
import com.ghyinc.finance.domain.loan.dto.ResultResponse
import com.ghyinc.finance.domain.loan.enums.PartnerCode
import com.ghyinc.finance.domain.loan.enums.PartnerInquiryStatus
import com.ghyinc.finance.domain.loan.repository.LoanLimitProductResultRepository
import org.apache.kafka.common.errors.InvalidRequestException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LoanLimitResultService(
    private val resultAdaptorFactory: LoanLimitResultAdaptorFactory,
    private val loanLimitProductResultRepository: LoanLimitProductResultRepository
) {
    private val log = LoggerFactory.getLogger(LoanLimitResultService::class.java)

    @Transactional
    fun responseCompareLoanResult(requestPartnerCode: String, reqBody: JsonNode): ResultResponse {
        val partnerCode = runCatching { PartnerCode.valueOf(requestPartnerCode) }
            .getOrElse { throw InvalidRequestException("유효하지 않은 partnerCode. PartnerCode: $requestPartnerCode") }

        val adaptor = resultAdaptorFactory.getAdaptor(partnerCode)

        try {
            val request = adaptor.convert(reqBody)

            request.loanApplyResults.forEach { item ->
                //loReqtNo와 productCode로 선저장된 ProductResult 조회
                val productResult =
                    loanLimitProductResultRepository.findByLoReqtNoAndProductCode(item.loReqtNo, item.productCode)
                        .orElseThrow { InvalidRequestException("존재하지 않는 식별번호&상품코드. loReqtNo: ${item.loReqtNo}, productCode: ${item.productCode}") }

                //비관적 Lock으로 동시 수신 시 순차 처리 보장
                val loanLimitInquiry = loanLimitProductResultRepository.findInquiryByLoReqtNoAndProduceCodeWithLock(
                    item.loReqtNo,
                    item.productCode
                )
                    .orElseThrow { InvalidRequestException("존재하지 않는 한도조회 이력") }

                //중복 or 처리불가 상태 체크
                if (productResult.status != PartnerInquiryStatus.SEND_SUCCESS) {
                    log.warn(
                        "[{}] 처리 불가 상태의 결과 수신. loReqtNo={}, status={}",
                        partnerCode, item.loReqtNo, productResult.status
                    )

                    if (productResult.status == PartnerInquiryStatus.SUCCESS) {
                        log.warn("[{}] 중복 수신. loReqtNo={}", partnerCode, item.loReqtNo)
                    }

                    return@forEach
                }

                //한도결과 UPDATE
                loanLimitInquiry.incrementSuccessCount()
                productResult.updateResult(item.resultCode, item.amount ?: 0L, item.interestRate ?: 0.0)
            }

            return adaptor.buildResponse(true, "한도결과 API 정상 처리")
        } catch (e: InvalidRequestException) {
            log.error("[{}] 한도결과 API 처리 중 오류. message={}", requestPartnerCode, e.message)
            return adaptor.buildResponse(false, e.message!!)
        } catch (e: Exception) {
            log.error("[{}] 한도결과 API 처리 중 오류. ", requestPartnerCode, e)
            return adaptor.buildResponse(false, "처리 중 오류가 발생했습니다")
        }
    }
}
