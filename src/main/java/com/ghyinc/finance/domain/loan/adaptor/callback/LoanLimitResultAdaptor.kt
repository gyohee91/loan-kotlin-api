package com.ghyinc.finance.domain.loan.adaptor.callback

import com.fasterxml.jackson.databind.JsonNode
import com.ghyinc.finance.domain.loan.dto.LoanLimitResultRequest
import com.ghyinc.finance.domain.loan.dto.ResultResponse
import com.ghyinc.finance.domain.loan.enums.PartnerCode

interface LoanLimitResultAdaptor {
    fun supports(partnerCode: PartnerCode): Boolean
    fun convert(body: JsonNode): LoanLimitResultRequest //원문 -> 표준 DTO
    fun buildResponse(success: Boolean, resultMessage: String): ResultResponse
}
