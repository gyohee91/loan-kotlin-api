package com.ghyinc.finance.domain.loan.adaptor.impl

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.ghyinc.finance.domain.loan.adaptor.callback.LoanLimitResultAdaptor
import com.ghyinc.finance.domain.loan.dto.LoanLimitResultRequest
import com.ghyinc.finance.domain.loan.dto.LoanLimitResultRequest.LoanApplyResult
import com.ghyinc.finance.domain.loan.dto.ResultResponse
import com.ghyinc.finance.domain.loan.enums.LoanLimitResultCode
import com.ghyinc.finance.domain.loan.enums.PartnerCode
import org.springframework.stereotype.Component
import java.util.Map

@Component
class TossbankResultAdaptor(
    private val objectMapper: ObjectMapper
) : LoanLimitResultAdaptor {

    private data class TossbankResultRequest(
        val preScreeningResult: List<PreScreeningResult>
    )

    private data class PreScreeningResult(
        val result: String,
        val loanReqNo: String,
        val loanProductId: String,
        val interestRate: Double,
        val amount: Long
    )

    private data class TossbankResultResponse(
        val code: String,
        val data: Map<String, Any>?
    ) : ResultResponse

    override fun supports(partnerCode: PartnerCode): Boolean =
        partnerCode == PartnerCode.TOSS_BANK


    override fun convert(body: JsonNode): LoanLimitResultRequest {
        val tossbankRequest: TossbankResultRequest =
            objectMapper.convertValue(body, TossbankResultRequest::class.java)

        val preScrResultLists =
            tossbankRequest.preScreeningResult.map { item ->
                LoanApplyResult.create(
                    loReqtNo = item.loanReqNo,
                    productCode = item.loanProductId,
                    resultCode = RESULT_CODE_MAP.getOrDefault(item.result, LoanLimitResultCode.UNKNOWN_ERROR),
                    amount = item.amount,
                    interestRate = item.interestRate
                )
            }

        return LoanLimitResultRequest.create(preScrResultLists)
    }

    override fun buildResponse(success: Boolean, resultMessage: String): ResultResponse {
        return TossbankResultResponse(
            code = if (success) "TEL000" else "TEL999",
            data = null
        )
    }

    companion object {
        private val RESULT_CODE_MAP = mapOf(
            "TLA00" to LoanLimitResultCode.SUCCESS,
            "TLA04" to LoanLimitResultCode.LIMIT_DENIED,
            "TLA06" to LoanLimitResultCode.DUPLICATE_REQUEST,
            "TLA02" to LoanLimitResultCode.INVALID_PRODUCT,
            "TLA10" to LoanLimitResultCode.PARTNER_SYSTEM_ERROR,
            "TLA08" to LoanLimitResultCode.TIMEOUT,
            "TLA99" to LoanLimitResultCode.UNKNOWN_ERROR
        )
    }
}
