package com.ghyinc.finance.domain.loan.adaptor.common

import com.ghyinc.finance.domain.external.nice.dto.AutoInfo
import com.ghyinc.finance.domain.external.nice.dto.AutoSecondInfo
import com.ghyinc.finance.domain.loan.adaptor.dto.LoanLimitAdaptorRequest
import com.ghyinc.finance.domain.loan.adaptor.dto.LoanLimitAdaptorResponse
import com.ghyinc.finance.domain.loan.adaptor.dto.LoanLimitAdaptorResponse.Companion.fail
import com.ghyinc.finance.domain.loan.adaptor.dto.LoanLimitAdaptorResponse.Companion.success
import com.ghyinc.finance.domain.loan.adaptor.impl.LoanLimitAdaptor
import com.ghyinc.finance.domain.loan.dto.RequestProduct
import com.ghyinc.finance.domain.loan.enums.JobType
import com.ghyinc.finance.domain.loan.enums.PartnerCode
import com.ghyinc.finance.global.client.ApiClientFactory
import com.ghyinc.finance.global.config.PartnerApiProperties
import com.ghyinc.finance.global.crypto.CryptoFactory
import io.github.resilience4j.circuitbreaker.CallNotPermittedException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class CommonLoanLimitAdaptor(
    private val apiClientFactory: ApiClientFactory,
    private val cryptoFactory: CryptoFactory,
    private val partnerApiProperties: PartnerApiProperties
) : LoanLimitAdaptor {
    private val log = LoggerFactory.getLogger(CommonLoanLimitAdaptor::class.java)

    private data class CommonLimitRequest(
        val requestProducts: List<RequestProduct>,
        val rrn: String?,
        val name: String?,
        val jobType: JobType?,
        val jobName: String?,
        val joinDate: String? = null,
        val carNo: String? = null,
        val autoInfo: AutoInfo? = null,
        val autoSecondInfo: AutoSecondInfo? = null
    )

    private data class CommonLimitResponse(
        val resultCode: String
    )

    override fun supports(partnerCode: PartnerCode): Boolean = partnerCode.standard

    override fun inquireLimit(
        partnerCode: PartnerCode,
        requestParam: LoanLimitAdaptorRequest
    ): LoanLimitAdaptorResponse {
        val startTime = System.currentTimeMillis()

        //통신 방식에 맞는 ApiClient 자동 선택
        val apiClient = apiClientFactory.getApiClient(partnerCode)
        val cryptoService = cryptoFactory.getCryptoService(partnerCode)
        val path = partnerApiProperties.getConfig(partnerCode).path

        try {
            val request = CommonLimitRequest(
                requestProducts = requestParam.requestProducts,
                rrn = cryptoService.encrypt(requestParam.rrno),
                name = cryptoService.encrypt(requestParam.name),
                jobType = requestParam.jobType,
                jobName = requestParam.jobName,
                joinDate = requestParam.joinDate,
                carNo = requestParam.carNo,
                autoInfo = requestParam.autoInfo,
                autoSecondInfo = requestParam.autoSecondInfo
            )

            val result = apiClient.post(
                partnerCode,
                path,
                request,
                CommonLimitResponse::class.java
            )

            val resTimeMs = System.currentTimeMillis() - startTime

            if ("SUCCESS" != result.resultCode) {
                log.warn("[{}] 한도조회 실패. resultCode={}", partnerCode, result.resultCode)
                return fail(
                    partnerCode = partnerCode,
                    failReason = result.resultCode,
                    resTimeMs = resTimeMs
                )
            }

            log.info("[{}] 한도조회 성공, resTimeMs={}", partnerCode, resTimeMs)

            return success(
                partnerCode = partnerCode,
                resTimeMs = resTimeMs
            )
        } catch (_: CallNotPermittedException) {
            // Circuit Breaker OPEN Fallback
            // -> 해당 금융사 격리, 나머지 금융사 정상 진행 (Partial Success)
            val resTimeMs = System.currentTimeMillis() - startTime
            log.warn("[{}] Circuit Breaker OPEN -> Fallback 실행", partnerCode)
            return fail(
                partnerCode = partnerCode,
                failReason = "CB_OPEN",
                resTimeMs = resTimeMs
            )
        } catch (e: Exception) {
            val resTimeMs = System.currentTimeMillis() - startTime
            log.error("[{}] 한도조회 오류 발생", partnerCode, e)
            return fail(
                partnerCode = partnerCode,
                failReason = e.message ?: "Unknown error",
                resTimeMs = resTimeMs
            )
        }
    }
}
