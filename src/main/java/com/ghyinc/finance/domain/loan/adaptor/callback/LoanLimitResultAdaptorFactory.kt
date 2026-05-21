package com.ghyinc.finance.domain.loan.adaptor.callback

import com.ghyinc.finance.domain.loan.enums.PartnerCode
import org.apache.kafka.common.errors.InvalidRequestException
import org.springframework.stereotype.Component

@Component
class LoanLimitResultAdaptorFactory(
    private val adaptors: List<LoanLimitResultAdaptor>
) {
    fun getAdaptor(partnerCode: PartnerCode): LoanLimitResultAdaptor =
        adaptors.firstOrNull { it.supports(partnerCode) }
            ?: throw InvalidRequestException("지원하지 않는 금융사입니다: $partnerCode")
}
