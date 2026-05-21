package com.ghyinc.finance.domain.loan.strategy

import com.ghyinc.finance.domain.loan.adaptor.dto.LoanLimitAdaptorRequest
import com.ghyinc.finance.domain.loan.dto.ExternalDataContext
import com.ghyinc.finance.domain.loan.dto.ExternalDataContext.Companion.empty
import com.ghyinc.finance.domain.loan.dto.LoanLimitRequest
import com.ghyinc.finance.domain.loan.enums.LoanType
import com.ghyinc.finance.domain.loan.enums.PartnerCode
import com.ghyinc.finance.domain.loan.repository.PartnerLoanTypeRepository
import com.ghyinc.finance.global.common.DateUtils.toDateTimeString
import org.springframework.stereotype.Component

@Component
class BusinessLoanLimitStrategy(
    private val partnerLoanTypeRepository: PartnerLoanTypeRepository
) : LoanLimitStrategy {

    override val loanType: LoanType = LoanType.BUSINESS

    override val supportedBanks: MutableList<PartnerCode>
        get() = partnerLoanTypeRepository.findActivePartnerCodeByLoanType(this.loanType)

    override fun validate(request: LoanLimitRequest) {
    }

    override fun fetchExternalData(request: LoanLimitRequest): ExternalDataContext {
        return empty()
    }

    override fun toAdaptorRequest(
        request: LoanLimitRequest,
        externalDataContext: ExternalDataContext
    ): LoanLimitAdaptorRequest {
        return LoanLimitAdaptorRequest(
            name = request.name,
            rrno = request.rrno,
            jobType = request.jobType,
            jobName = request.jobName,
            joinDate = request.joinDate,
            loanType = request.loanType,
            carNo = request.carNo,
            agreePersonalCreditInfo = request.agreePersonalCreditInfo,
            agreePersonalCreditTime = toDateTimeString(request.agreePersonalCreditTime)
        )
    }

    override fun requiresExternalData(): Boolean = false
}
