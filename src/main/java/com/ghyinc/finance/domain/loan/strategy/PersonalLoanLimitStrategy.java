package com.ghyinc.finance.domain.loan.strategy;

import com.ghyinc.finance.domain.loan.adaptor.dto.LoanLimitAdaptorRequest;
import com.ghyinc.finance.domain.loan.dto.ExternalDataContext;
import com.ghyinc.finance.domain.loan.dto.LoanLimitRequest;
import com.ghyinc.finance.domain.loan.enums.LoanType;
import com.ghyinc.finance.domain.loan.enums.PartnerCode;
import com.ghyinc.finance.domain.loan.repository.PartnerLoanTypeRepository;
import com.ghyinc.finance.global.common.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PersonalLoanLimitStrategy implements LoanLimitStrategy {
    private final PartnerLoanTypeRepository partnerLoanTypeRepository;

    @Override
    public LoanType getLoanType() {
        return LoanType.PERSONAL_CREDIT;
    }

    @Override
    public List<PartnerCode> getSupportedBanks() {
        return partnerLoanTypeRepository.findActivePartnerCodeByLoanType(this.getLoanType());
    }

    @Override
    public void validate(LoanLimitRequest request) {

    }

    @Override
    public ExternalDataContext fetchExternalData(LoanLimitRequest request) {
        return ExternalDataContext.empty();
    }

    @Override
    public LoanLimitAdaptorRequest toAdaptorRequest(LoanLimitRequest request, ExternalDataContext externalDataContext) {
        return LoanLimitAdaptorRequest.create(
                request.name(),
                request.rrno(),
                request.jobType(),
                request.jobName(),
                request.joinDate(),
                request.loanType(),
                null,
                null,
                request.agreePersonalCreditInfo(),
                DateUtils.toDateTimeString(request.agreePersonalCreditTime()),
                null,
                null,
                null
        );
    }

    @Override
    public boolean requiresExternalData() {
        return false;
    }
}
