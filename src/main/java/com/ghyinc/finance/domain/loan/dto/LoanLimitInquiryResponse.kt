package com.ghyinc.finance.domain.loan.dto

import com.ghyinc.finance.domain.loan.entity.LoanLimitInquiry
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "금리 한도조회 (응답)")
data class LoanLimitInquiryResponse(
    @field:Schema(description = "업무 식별번호")
    val inquiryNo: String? = null,

    @field:Schema(description = "성공 여부")
    val success: Boolean = false
) {
    companion object {
        @JvmStatic
        fun from(inquiry: LoanLimitInquiry): LoanLimitInquiryResponse =
            LoanLimitInquiryResponse(
                inquiryNo = inquiry.inquiryNo,
                success = true
            )
    }
}
