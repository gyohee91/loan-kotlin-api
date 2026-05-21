package com.ghyinc.finance.domain.loan.factory

import com.ghyinc.finance.domain.loan.adaptor.impl.LoanLimitAdaptor
import com.ghyinc.finance.domain.loan.enums.PartnerCode
import org.apache.kafka.common.errors.InvalidRequestException
import org.springframework.stereotype.Component

/**
 * 금융사 코드에 따른 Adaptor 반환 팩토리
 *
 *
 * Strategy와 동일하게 Spring DI로 Adaptor 구현체를 자동 수집
 * 새로운 금융사 연동 시 Adaptor 구현체만 추가하면 됨.
 */
@Component
class LoanLimitAdaptorFactory(
    private val adaptors: List<LoanLimitAdaptor>
) {

    fun getAdaptor(partnerCode: PartnerCode): LoanLimitAdaptor =
        adaptors.firstOrNull { it.supports(partnerCode) }
            ?: throw InvalidRequestException("지원하지 않는 금융사입니다. $partnerCode")
}
