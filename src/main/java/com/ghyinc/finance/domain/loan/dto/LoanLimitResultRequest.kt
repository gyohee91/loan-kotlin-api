package com.ghyinc.finance.domain.loan.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.ghyinc.finance.domain.loan.enums.LoanLimitResultCode

class LoanLimitResultRequest(
    @field:JsonProperty("loanApplyResults")
    val loanApplyResults: List<LoanApplyResult> = emptyList()
) {
    class LoanApplyResult(
        val loReqtNo: String,
        val productCode: String,
        val resultCode: LoanLimitResultCode,
        val amount: Long? = null,
        val interestRate: Double? = null,
    ) {
        companion object {
            @JvmStatic
            fun create(
                loReqtNo: String,
                productCode: String,
                resultCode: LoanLimitResultCode,
                amount: Long? = null,
                interestRate: Double? = null
            ): LoanApplyResult =
                LoanApplyResult(
                    loReqtNo = loReqtNo,
                    productCode = productCode,
                    resultCode = resultCode,
                    amount = amount,
                    interestRate = interestRate
                )
        }
    }

    companion object {
        @JvmStatic
        fun create(
            loanApplyResults: List<LoanApplyResult>
        ): LoanLimitResultRequest =
            LoanLimitResultRequest(
                loanApplyResults = loanApplyResults
            )
    }
}
