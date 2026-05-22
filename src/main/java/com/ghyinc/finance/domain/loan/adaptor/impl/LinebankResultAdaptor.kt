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

@Component
class LinebankResultAdaptor(
    private val objectMapper: ObjectMapper
) : LoanLimitResultAdaptor {

    private data class LinebankResultRequest(
        val preScreeningResult: List<PreScreeningResult>
    )

    private data class PreScreeningResult(
        val result: String,
        val ticketId: String,
        val productName: String,
        val loanProductId: String,
        val interestRate: String,
        val amount: String
    )

    private data class LinebankResultResponse(
        val success: Boolean,
        val data: Map<String, Any>?
    ) : ResultResponse

    override fun supports(partnerCode: PartnerCode): Boolean =
        partnerCode == PartnerCode.LINE_BANK

    override fun convert(body: JsonNode): LoanLimitResultRequest {
        val linebankResultRequest: LinebankResultRequest =
            objectMapper.convertValue(body, LinebankResultRequest::class.java)

        val preScreeningResult =
            linebankResultRequest.preScreeningResult.map { item ->
                    LoanApplyResult.create(
                        loReqtNo = item.ticketId,
                        productCode = item.loanProductId,
                        resultCode = RESULT_CODE_MAP.getOrDefault(item.result, LoanLimitResultCode.UNKNOWN_ERROR),
                        amount = item.amount.toLong(),
                        interestRate = item.interestRate.toDouble()
                    )
                }

        return LoanLimitResultRequest.create(preScreeningResult)
    }

    override fun buildResponse(success: Boolean, resultMessage: String): ResultResponse {
        return LinebankResultResponse(
            success,
            null
        )
    }

    companion object {
        private val RESULT_CODE_MAP = mapOf(
            "NFLA00" to LoanLimitResultCode.SUCCESS,
            "NFLA01" to LoanLimitResultCode.LIMIT_DENIED,
            "NFLA02" to LoanLimitResultCode.DUPLICATE_REQUEST,
            "NFLA05" to LoanLimitResultCode.INVALID_PRODUCT,
            "NFLA95" to LoanLimitResultCode.PARTNER_SYSTEM_ERROR,
            "NFLA99" to LoanLimitResultCode.UNKNOWN_ERROR
        )
    }
}
