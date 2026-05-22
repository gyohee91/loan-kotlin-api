package com.ghyinc.finance.global.event

import com.ghyinc.finance.domain.loan.adaptor.dto.LoanLimitAdaptorRequest
import com.ghyinc.finance.domain.loan.enums.PartnerCode

/**
 * 한도조회 LoanLimitSenderService.inquiry에 대한 이벤트 DTO
 */
class LoanLimitInquiryCreatedEvent(
    val id: Long? = null,
    val activePartnerCodes: List<PartnerCode>,
    val adaptorRequest: LoanLimitAdaptorRequest
) {
    companion object {
        @JvmStatic
        fun create(
            id: Long ? = null,
            activePartnerCodes: List<PartnerCode>,
            adaptorRequest: LoanLimitAdaptorRequest
        ): LoanLimitInquiryCreatedEvent =
            LoanLimitInquiryCreatedEvent(
                id = id,
                activePartnerCodes = activePartnerCodes,
                adaptorRequest = adaptorRequest
            )
    }
}
