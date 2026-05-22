package com.ghyinc.finance.domain.loan.adaptor.common

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.ghyinc.finance.domain.loan.adaptor.callback.LoanLimitResultAdaptor
import com.ghyinc.finance.domain.loan.dto.LoanLimitResultRequest
import com.ghyinc.finance.domain.loan.dto.LoanLimitResultResponse.Companion.fail
import com.ghyinc.finance.domain.loan.dto.LoanLimitResultResponse.Companion.success
import com.ghyinc.finance.domain.loan.dto.ResultResponse
import com.ghyinc.finance.domain.loan.enums.PartnerCode
import lombok.extern.slf4j.Slf4j
import org.springframework.stereotype.Component

@Slf4j
@Component
class CommonResultAdaptor(
    private val objectMapper: ObjectMapper
) : LoanLimitResultAdaptor {

    override fun supports(partnerCode: PartnerCode): Boolean {
        return partnerCode.standard
    }

    override fun convert(body: JsonNode): LoanLimitResultRequest {
        //표준 Layout은 그대로 역직렬화
        return objectMapper.convertValue(body, LoanLimitResultRequest::class.java)
    }

    override fun buildResponse(success: Boolean, resultMessage: String): ResultResponse {
        return if (success)
            success()
        else
            fail(resultMessage)
    }
}
