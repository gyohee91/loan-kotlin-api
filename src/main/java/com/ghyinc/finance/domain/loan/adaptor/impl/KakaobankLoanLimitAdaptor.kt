package com.ghyinc.finance.domain.loan.adaptor.impl

import com.fasterxml.jackson.annotation.JsonProperty
import com.ghyinc.finance.domain.external.nice.dto.AutoInfo
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
class KakaobankLoanLimitAdaptor(
    private val apiClientFactory: ApiClientFactory,
    private val cryptoFactory: CryptoFactory,
    private val partnerApiProperties: PartnerApiProperties
) : LoanLimitAdaptor {
    private val log = LoggerFactory.getLogger(KakaobankLoanLimitAdaptor::class.java)

    private data class KakaobankLimitRequest(
        @field:JsonProperty("alnc_gds_infos") val alncGdsInfos: List<AlncGdsInfo>,
        @field:JsonProperty("rsdt_no") val rsdtNo: String,
        @field:JsonProperty("cust_nm") val custNm: String,
        @field:JsonProperty("cust_input_info") val custInputInfo: CustInputInfo,
        @field:JsonProperty("vhc_no") val vhcNo: String? = null,
        @field:JsonProperty("car_parts") val carParts: CarParts? = null
    )

    private data class AlncGdsInfo(
        @field:JsonProperty("iqry_dman_no") val iqryDmanNo: String? = null,
        @field:JsonProperty("alnc_gds_unq_cd") val alncGdsUnqCd: String
    )

    private data class CustInputInfo(
        @field:JsonProperty("ocup_dvcd") val ocupDvcd: String?,
        @field:JsonProperty("cur_wrst_nm") val curWrstNm: String?,
        @field:JsonProperty("cur_wrst_encm") val curWrstEncm: String?
    ) {
        companion object {
            fun from(requestParam: LoanLimitAdaptorRequest): CustInputInfo {
                return CustInputInfo(
                    ocupDvcd = requestParam.jobType?.name,
                    curWrstNm = requestParam.jobName,
                    curWrstEncm = requestParam.joinDate
                )
            }
        }
    }

    private data class CarParts(
        val seq: String?,
        val formKind: String?,
        val resCarNo: String?,
        val seatingCapacity: String?,
        val resMotorType: String?,
        val resUseType: String?,
        val resCarModelType: String?
    ) {
        companion object {
            fun from(autoInfo: AutoInfo?): CarParts? {
                autoInfo ?: return null
                return CarParts(
                    seq = autoInfo.seq,
                    formKind = autoInfo.formKind,
                    resCarNo = autoInfo.resCarNo,
                    seatingCapacity = autoInfo.seatingCapacity,
                    resMotorType = autoInfo.resMotorType,
                    resUseType = autoInfo.resUseType,
                    resCarModelType = autoInfo.resCarModelType
                )
            }
        }
    }

    private data class LimitResponse(
        val resultCode: String
    )


    override fun supports(partnerCode: PartnerCode): Boolean =
        partnerCode == PartnerCode.KAKAO_BANK

    override fun inquireLimit(
        partnerCode: PartnerCode,
        requestParam: LoanLimitAdaptorRequest
    ): LoanLimitAdaptorResponse {
        val startTime = System.currentTimeMillis()

        val cryptoService = cryptoFactory.getCryptoService(partnerCode)
        val apiClient = apiClientFactory.getApiClient(partnerCode)
        val path = partnerApiProperties.getConfig(PartnerCode.KAKAO_BANK).path

        try {
            val request = KakaobankLimitRequest(
                alncGdsInfos = requestParam.requestProducts.map { requestProduct ->
                            AlncGdsInfo(
                                iqryDmanNo = requestProduct.loReqtNo,
                                alncGdsUnqCd = requestProduct.productCode
                            )
                        },
                rsdtNo = cryptoService.encrypt(requestParam.rrno),
                custNm = cryptoService.encrypt(requestParam.name),
                custInputInfo = CustInputInfo.from(requestParam),
                vhcNo = requestParam.carNo,
                carParts = CarParts.from(requestParam.autoInfo)
            )

            val result = apiClient.post(
                partnerCode,
                path,
                request,
                LimitResponse::class.java
            )

            val resTimeMs = System.currentTimeMillis() - startTime

            if ("SUCCESS" != result.resultCode) {
                log.warn("[{}] 한도조회 실패. resultCode={}", PartnerCode.KAKAO_BANK, result.resultCode)
                return fail(
                    partnerCode = PartnerCode.KAKAO_BANK,
                    failReason = result.resultCode,
                    resTimeMs = resTimeMs
                )
            }

            log.info("[{}] 한도조회 성공, resTimeMs={}", PartnerCode.KAKAO_BANK, resTimeMs)

            return success(
                partnerCode = PartnerCode.KAKAO_BANK,
                resTimeMs = resTimeMs
            )
        } catch (_: CallNotPermittedException) {
            // Circuit Breaker OPEN Fallback
            // -> 해당 금융사 격리, 나머지 금융사 정상 진행 (Partial Success)
            val resTimeMs = System.currentTimeMillis() - startTime
            log.warn("[{}] Circuit Breaker OPEN -> Fallback 실행", PartnerCode.KAKAO_BANK)
            return fail(
                partnerCode = partnerCode,
                failReason = "CB_OPEN",
                resTimeMs = resTimeMs
            )
        } catch (e: Exception) {
            val resTimeMs = System.currentTimeMillis() - startTime
            log.error("[{}] 한도조회 오류 발생", PartnerCode.KAKAO_BANK, e)
            return fail(
                partnerCode = PartnerCode.KAKAO_BANK,
                failReason = e.message ?: "Unknown error",
                resTimeMs = resTimeMs
            )
        }
    }
}
