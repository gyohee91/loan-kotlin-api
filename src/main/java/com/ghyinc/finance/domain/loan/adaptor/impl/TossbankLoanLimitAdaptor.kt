package com.ghyinc.finance.domain.loan.adaptor.impl

import com.fasterxml.jackson.databind.ObjectMapper
import com.ghyinc.finance.domain.loan.adaptor.dto.LoanLimitAdaptorRequest
import com.ghyinc.finance.domain.loan.adaptor.dto.LoanLimitAdaptorResponse
import com.ghyinc.finance.domain.loan.adaptor.dto.LoanLimitAdaptorResponse.Companion.fail
import com.ghyinc.finance.domain.loan.adaptor.dto.LoanLimitAdaptorResponse.Companion.success
import com.ghyinc.finance.domain.loan.enums.PartnerCode
import com.ghyinc.finance.global.client.ApiClientFactory
import com.ghyinc.finance.global.config.PartnerApiProperties
import com.ghyinc.finance.global.crypto.CryptoFactory
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Slf4j
@Component
class TossbankLoanLimitAdaptor(
    private val apiClientFactory: ApiClientFactory,
    private val cryptoFactory: CryptoFactory,
    private val partnerApiProperties: PartnerApiProperties,
    private val objectMapper: ObjectMapper
) : LoanLimitAdaptor {
    private val log = LoggerFactory.getLogger(TossbankLoanLimitAdaptor::class.java)

    private data class TossbankLimitRequest(
        val encrytedData: String
    )

    private data class LimitRequestPlainText(
        val data: Data,
        val requestProducts: List<RequestProduct>
    )

    private data class Data(
        val agreePersonalCreditInfo: Boolean? = null,
        val agreeTermsTime: String? = null,
        val name: String,
        val jobType: String?,
        val joinDate: String?,
        val rrn: String,
        val corporateName: String?,
        val automobileNumber: String? = null,
        val automobileInfo: AutomobileInfo? = null
    )

    private data class AutomobileInfo(
        val seq: String?,
        val formKind: String?,
        val resCarNo: String?,
        val seatingCapacity: String?,
        val resMotorType: String?,
        val resUseType: String?,
        val resCarModelType: String?
    )

    private data class RequestProduct(
        val loanReqNo: String? = null,
        val loanProductId: String
    )

    private data class TossbankLimitResponse(
        val resultCode: String
    )


    override fun supports(partnerCode: PartnerCode): Boolean =
        partnerCode == PartnerCode.TOSS_BANK

    override fun inquireLimit(
        partnerCode: PartnerCode,
        requestParam: LoanLimitAdaptorRequest
    ): LoanLimitAdaptorResponse {
        val startTime = System.currentTimeMillis()

        val apiClient = apiClientFactory.getApiClient(partnerCode)
        val cryptoService = cryptoFactory.getCryptoService(partnerCode)
        val path = partnerApiProperties.getConfig(partnerCode).path

        return try {
            val limitRequestPlainText = LimitRequestPlainText(
                data = Data(
                    rrn = requestParam.rrno,
                    name = requestParam.name,
                    jobType = requestParam.jobType?.name,
                    joinDate = requestParam.joinDate,
                    corporateName = requestParam.jobName,
                    automobileNumber = requestParam.carNo
                ),
                requestProducts =
                    requestParam.requestProducts.map { requestProduct ->
                        RequestProduct(
                            loanReqNo = requestProduct.loReqtNo,
                            loanProductId = requestProduct.productCode
                        )
                    }
            )

            // Tossbank는 json 전체 암호화
            val plainText = objectMapper.writeValueAsString(limitRequestPlainText)
            val request = TossbankLimitRequest(encrytedData = cryptoService.encrypt(plainText))

            //External API
            val result = apiClient.post(
                partnerCode,
                path,
                request,
                TossbankLimitResponse::class.java
            )

            val resTimeMs = System.currentTimeMillis() - startTime

            if ("SUCCESS" != result.resultCode) {
                log.warn("[{}] 한도조회 실패. resultCode={}", PartnerCode.TOSS_BANK, result.resultCode)
                fail(
                    partnerCode = PartnerCode.TOSS_BANK,
                    failReason = result.resultCode,
                    resTimeMs = resTimeMs
                )
            }

            log.info("[{}] 한도조회 성공, resTimeMs={}", PartnerCode.TOSS_BANK, resTimeMs)

            success(
                partnerCode = PartnerCode.TOSS_BANK,
                resTimeMs = resTimeMs
            )
        } catch (_: CallNotPermittedException) {
            // Circuit Breaker OPEN Fallback
            // -> 해당 금융사 격리, 나머지 금융사 정상 진행 (Partial Success)
            val resTimeMs = System.currentTimeMillis() - startTime
            log.warn("[{}] Circuit Breaker OPEN -> Fallback 실행", PartnerCode.TOSS_BANK)
            fail(
                partnerCode = PartnerCode.TOSS_BANK,
                failReason = "CB_OPEN",
                resTimeMs = resTimeMs
            )
        } catch (e: Exception) {
            val resTimeMs = System.currentTimeMillis() - startTime
            log.error("[{}] 한도조회 오류 발생", PartnerCode.TOSS_BANK, e)
            fail(
                partnerCode = PartnerCode.TOSS_BANK,
                failReason = e.message ?: "Unknown error",
                resTimeMs = resTimeMs
            )
        }
    }
}
