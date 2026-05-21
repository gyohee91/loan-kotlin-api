package com.ghyinc.finance.domain.loan.adaptor.dto

import com.ghyinc.finance.domain.loan.enums.PartnerCode

data class LoanLimitAdaptorResponse(
    val partnerCode: PartnerCode,
    val success: Boolean,
    val failReason: String? = null,
    val resTimeMs: Long
) {
    companion object {
        @JvmStatic
        fun success(
            partnerCode: PartnerCode,
            resTimeMs: Long
        ): LoanLimitAdaptorResponse =
            LoanLimitAdaptorResponse(
                partnerCode = partnerCode,
                success = true,
                failReason = null,
                resTimeMs = resTimeMs
            )

        @JvmStatic
        fun fail(
            partnerCode: PartnerCode,
            failReason: String,
            resTimeMs: Long
        ): LoanLimitAdaptorResponse =
            LoanLimitAdaptorResponse(
                partnerCode = partnerCode,
                success = false,
                failReason = failReason,
                resTimeMs = resTimeMs
            )
    }
}
