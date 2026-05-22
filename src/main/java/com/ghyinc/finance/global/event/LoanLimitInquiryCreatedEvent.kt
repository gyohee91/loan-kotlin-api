package com.ghyinc.finance.global.event;

import com.ghyinc.finance.domain.loan.adaptor.dto.LoanLimitAdaptorRequest;
import com.ghyinc.finance.domain.loan.enums.PartnerCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class LoanLimitInquiryCreateEvent {
    private Long id;
    private List<PartnerCode> activePartnerCodes;
    private LoanLimitAdaptorRequest adaptorRequest;
}
