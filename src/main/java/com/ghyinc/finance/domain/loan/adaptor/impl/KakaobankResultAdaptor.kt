package com.ghyinc.finance.domain.loan.adaptor.impl

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.ghyinc.finance.domain.loan.adaptor.callback.LoanLimitResultAdaptor
import com.ghyinc.finance.domain.loan.dto.LoanLimitResultRequest
import com.ghyinc.finance.domain.loan.dto.LoanLimitResultRequest.LoanApplyResult
import com.ghyinc.finance.domain.loan.dto.ResultResponse
import com.ghyinc.finance.domain.loan.enums.LoanLimitResultCode
import com.ghyinc.finance.domain.loan.enums.PartnerCode
import org.springframework.stereotype.Component

@Component
class KakaobankResultAdaptor(
    private val objectMapper: ObjectMapper
) : LoanLimitResultAdaptor {

    private data class KakaobankResultRequest(
        val products: List<Product>
    )

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
    private data class Product(
        val iqryDmanNo: String,
        val alncGdsUnqCd: String,
        val rsltCd: String,
        val loanLimitAmt: Long,
        val lastLoanIntr: Double,
        val loanTrmMcnt: String
    )

    @JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
    private data class KakaobankResultResponse(
        val rsltCd: String
    ) : ResultResponse

    override fun supports(partnerCode: PartnerCode): Boolean =
        partnerCode == PartnerCode.KAKAO_BANK

    override fun convert(body: JsonNode): LoanLimitResultRequest {
        val kakaobankRequest: KakaobankResultRequest =
            objectMapper.convertValue(body, KakaobankResultRequest::class.java)

        val preScrResultLists = kakaobankRequest.products.map { item ->
                LoanApplyResult.create(
                    loReqtNo = item.iqryDmanNo,
                    productCode = item.alncGdsUnqCd,
                    resultCode = RESULT_CODE_MAP.getOrDefault(item.rsltCd, LoanLimitResultCode.UNKNOWN_ERROR),
                    amount = item.loanLimitAmt,
                    interestRate = item.lastLoanIntr
                )
            }

        return LoanLimitResultRequest.create(preScrResultLists)
    }

    override fun buildResponse(success: Boolean, resultMessage: String): ResultResponse {
        return KakaobankResultResponse(
            if (success) "CP0000" else "CP9999"
        )
    }

    companion object {
        private val RESULT_CODE_MAP = mapOf(
            "CP0000" to LoanLimitResultCode.SUCCESS,
            "CP1009" to LoanLimitResultCode.LIMIT_DENIED,
            "CP1011" to LoanLimitResultCode.DUPLICATE_REQUEST,
            "CP4011" to LoanLimitResultCode.INVALID_PRODUCT,
            "CP5001" to LoanLimitResultCode.PARTNER_SYSTEM_ERROR,
            "CP5002" to LoanLimitResultCode.TIMEOUT,
            "CP5000" to LoanLimitResultCode.UNKNOWN_ERROR
        )
    }
}
