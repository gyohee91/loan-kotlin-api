package com.ghyinc.finance.domain.loan.strategy;

import com.ghyinc.finance.domain.external.nice.dto.NiceDnrResult;
import com.ghyinc.finance.domain.external.nice.service.NiceDnrService;
import com.ghyinc.finance.domain.loan.adaptor.dto.LoanLimitAdaptorRequest;
import com.ghyinc.finance.domain.loan.dto.ExternalDataContext;
import com.ghyinc.finance.domain.loan.dto.ExternalDataError;
import com.ghyinc.finance.domain.loan.dto.LoanLimitRequest;
import com.ghyinc.finance.domain.loan.enums.LoanType;
import com.ghyinc.finance.domain.loan.enums.PartnerCode;
import com.ghyinc.finance.domain.loan.repository.PartnerLoanTypeRepository;
import com.ghyinc.finance.global.common.DateUtils;
import com.ghyinc.finance.global.exception.ExternalApiFailException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.errors.InvalidRequestException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutoLoanLimitStrategy implements LoanLimitStrategy{
    private final NiceDnrService niceDnrService;
    private final PartnerLoanTypeRepository partnerLoanTypeRepository;

    @Override
    public LoanType getLoanType() {
        return LoanType.AUTO;
    }

    @Override
    public List<PartnerCode> getSupportedBanks() {
        return partnerLoanTypeRepository.findActivePartnerCodeByLoanType(this.getLoanType());
    }

    @Override
    public void validate(LoanLimitRequest request) {
        // 차량번호 필수 검증
        if(Objects.isNull(request.carNo()) || request.carNo().isBlank()) {
            throw new InvalidRequestException("오토담보 대출은 차량번호가 필수입니다");
        }
    }

    @Override
    public ExternalDataContext fetchExternalData(LoanLimitRequest request) {
        try {
            NiceDnrResult result = niceDnrService.inquireNiceDnr(request.carNo(), request.name());
            return ExternalDataContext.ofNiceDnr(result);
        } catch (ExternalApiFailException e) {
            log.error("Nice DNR 조회 실패. carNo={}", request.carNo(), e);

            // 예외를 던지지 않고 오류 정보만 context에 담아 return
            return ExternalDataContext.ofError(
                    "NICE_DNR",
                    ExternalDataError.create(
                            "NICE_DNR_ERROR",
                            e.getMessage()
                    )
            );
        }
    }

    @Override
    public LoanLimitAdaptorRequest toAdaptorRequest(LoanLimitRequest request, ExternalDataContext externalDataContext) {
        NiceDnrResult result = externalDataContext.niceDnrResult();
        return LoanLimitAdaptorRequest.create(
                request.name(),
                request.rrno(),
                request.jobType(),
                request.jobName(),
                request.joinDate(),
                request.loanType(),
                request.carNo(),
                "",
                request.agreePersonalCreditInfo(),
                DateUtils.toDateTimeString(request.agreePersonalCreditTime()),
                result.getAutoInfo(),
                result.getAutoSecondInfo(),
                null
        );
    }

    @Override
    public boolean requiresExternalData() {
        return true;
    }

    @Override
    public List<PartnerCode> filterAvailablePartners(List<PartnerCode> activePartnerCodes, ExternalDataContext context) {
        // Nice DNR 실패 시 차량정보가 필요한 금융사 제외
        if(!context.hasNiceDnrError()) {
            log.warn("Nice DNR 조회 실패로 오토담보 한도조회 불가");
            return List.of();
        }

        return activePartnerCodes;
    }
}
