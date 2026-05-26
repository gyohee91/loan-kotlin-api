package com.ghyinc.finance.domain.loan.adaptor.impl

import com.ghyinc.finance.domain.loan.adaptor.dto.LoanLimitAdaptorRequest
import com.ghyinc.finance.domain.loan.adaptor.dto.LoanLimitAdaptorResponse
import com.ghyinc.finance.domain.loan.adaptor.dto.LoanLimitAdaptorResponse.Companion.fail
import com.ghyinc.finance.domain.loan.adaptor.dto.LoanLimitAdaptorResponse.Companion.success
import com.ghyinc.finance.domain.loan.enums.PartnerCode
import com.ghyinc.finance.global.client.ApiClientFactory
import com.ghyinc.finance.global.config.PartnerApiProperties
import com.ghyinc.finance.global.crypto.CryptoFactory
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class LinebankLoanLimitAdaptor(
    private val apiClientFactory: ApiClientFactory,
    private val cryptoFactory: CryptoFactory,
    private val partnerApiProperties: PartnerApiProperties
) : LoanLimitAdaptor {
    private val log = LoggerFactory.getLogger(LinebankLoanLimitAdaptor::class.java)

    private data class LinebankLimitRequest(
        val preScreeningRequest: PreScreeningRequest
    )

    private data class PreScreeningRequest(
        val data: Data,
        val requestProducts: List<RequestProduct>
    )

    private data class Data(
        val agreePersonalCreditInfo: Boolean? = null,
        val agreeIdentifyInfo: Boolean? = null,
        val name: String,
        val rrn: String,
        val ci: String? = null,
        val authSmsTime: String? = null,
        val jobType: String?,
        val joinDate: String?,
        val carNo: String?
    )

    private data class RequestProduct(
        val ticketId: String? = null,
        val loanProductId: String
    )

    private data class LinebankLimitResponse(
        val resultCode: String
    )

    override fun supports(partnerCode: PartnerCode): Boolean =
        partnerCode == PartnerCode.LINE_BANK

    override fun inquireLimit(
        partnerCode: PartnerCode,
        requestParam: LoanLimitAdaptorRequest
    ): LoanLimitAdaptorResponse {
        val startTime = System.currentTimeMillis()

        val apiClient = apiClientFactory.getApiClient(partnerCode)
        val cryptoService = cryptoFactory.getCryptoService(partnerCode)
        val path = partnerApiProperties.getConfig(partnerCode).path

        return try {
            val preScreeningRequest = PreScreeningRequest(
                data = Data(
                    name = cryptoService.encrypt(requestParam.name),
                    rrn = cryptoService.encrypt(requestParam.rrno),
                    jobType = requestParam.jobType?.name,
                    joinDate = requestParam.joinDate,
                    carNo = requestParam.carNo
                ),
                requestProducts =
                    requestParam.requestProducts.map { requestProduct ->
                        RequestProduct(
                            ticketId = requestProduct.loReqtNo,
                            loanProductId = requestProduct.productCode
                        )
                    }
            )

            val request = LinebankLimitRequest(preScreeningRequest = preScreeningRequest)

            // External API
            val result = apiClient.post(
                partnerCode,
                path,
                request,
                LinebankLimitResponse::class.java
            )

            val resTimeMs = System.currentTimeMillis() - startTime

            if ("SUCCESS" != result.resultCode) {
                log.warn("[{}] 한도조회 실패. resultCode={}", PartnerCode.LINE_BANK, result.resultCode)
                fail(
                    partnerCode = PartnerCode.LINE_BANK,
                    failReason = result.resultCode,
                    resTimeMs = resTimeMs
                )
            }

            log.info("[{}] 한도조회 성공, resTimeMs={}", PartnerCode.LINE_BANK, resTimeMs)

            success(
                partnerCode = PartnerCode.LINE_BANK,
                resTimeMs = resTimeMs
            )
        } catch (_: CallNotPermittedException) {
            // Circuit Breaker OPEN Fallback
            // -> 해당 금융사 격리, 나머지 금융사 정상 진행 (Partial Success)
            val resTimeMs = System.currentTimeMillis() - startTime
            log.warn("[{}] Circuit Breaker OPEN -> Fallback 실행", PartnerCode.LINE_BANK)
            fail(
                partnerCode = partnerCode,
                failReason = "CB_OPEN",
                resTimeMs = resTimeMs
            )
        } catch (e: Exception) {
            val resTimeMs = System.currentTimeMillis() - startTime
            log.error("[{}] 한도조회 오류 발생", PartnerCode.LINE_BANK, e)
            fail(
                partnerCode = PartnerCode.LINE_BANK,
                failReason = e.message ?: "Unknown error",
                resTimeMs = resTimeMs
            )
        }
    }
}
