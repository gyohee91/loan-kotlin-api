package com.ghyinc.finance.domain.loan.dto

import lombok.Builder

@Builder
@JvmRecord
data class LoanLimitResultResponse(
    val resultCode: String,
    val resultMessage: String? = null
) : ResultResponse {
    companion object {
        @JvmStatic
        fun success(): LoanLimitResultResponse =
            LoanLimitResultResponse(
                resultCode = "SUCCESS"
            )

        @JvmStatic
        fun fail(resultMessage: String?): LoanLimitResultResponse =
            LoanLimitResultResponse(
                resultCode = "FAIL",
                resultMessage = resultMessage
            )
    }
}
