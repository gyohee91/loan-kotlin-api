package com.ghyinc.finance.global.event

import com.ghyinc.finance.domain.loan.enums.InquiryStatus
import lombok.Getter

@Getter
class LoanLimitCompletedEvent(
    val inquiryNo: String,
    val userId: Long,
    val name: String? = null,
    val status: InquiryStatus? = null,

    val requestId: String
) {
    companion object {
        @JvmStatic
        fun create(
            inquiryNo: String,
            userId: Long,
            name: String? = null,
            status: InquiryStatus? = null,
            requestId: String
        ): LoanLimitCompletedEvent =
            LoanLimitCompletedEvent(
                inquiryNo = inquiryNo,
                userId = userId,
                name = name,
                status = status,
                requestId = requestId
            )
    }
}
