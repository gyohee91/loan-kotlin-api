package com.ghyinc.finance.domain.loan.strategy

import com.ghyinc.finance.domain.external.nice.service.NiceDnrService
import com.ghyinc.finance.domain.loan.adaptor.dto.LoanLimitAdaptorRequest
import com.ghyinc.finance.domain.loan.dto.ExternalDataContext
import com.ghyinc.finance.domain.loan.dto.ExternalDataContext.Companion.ofError
import com.ghyinc.finance.domain.loan.dto.ExternalDataContext.Companion.ofNiceDnr
import com.ghyinc.finance.domain.loan.dto.ExternalDataError.Companion.create
import com.ghyinc.finance.domain.loan.dto.LoanLimitRequest
import com.ghyinc.finance.domain.loan.enums.LoanType
import com.ghyinc.finance.domain.loan.enums.PartnerCode
import com.ghyinc.finance.domain.loan.repository.PartnerLoanTypeRepository
import com.ghyinc.finance.global.common.DateUtils.toDateTimeString
import com.ghyinc.finance.global.exception.ExternalApiFailException
import lombok.extern.slf4j.Slf4j
import org.apache.kafka.common.errors.InvalidRequestException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Slf4j
@Component
class AutoLoanLimitStrategy(
    private val niceDnrService: NiceDnrService,
    private val partnerLoanTypeRepository: PartnerLoanTypeRepository
): LoanLimitStrategy {
    private val log = LoggerFactory.getLogger(AutoLoanLimitStrategy::class.java)

    override val loanType: LoanType = LoanType.AUTO

    override val supportedBanks: List<PartnerCode>
        get() = partnerLoanTypeRepository.findActivePartnerCodeByLoanType(this.loanType)

    override fun validate(request: LoanLimitRequest) {
        // 차량번호 필수 검증
        if (request.carNo.isNullOrBlank()) {
            throw InvalidRequestException("오토담보 대출은 차량번호가 필수입니다")
        }
    }

    override fun fetchExternalData(request: LoanLimitRequest): ExternalDataContext {
        return try {
            val result = niceDnrService.inquireNiceDnr(request.carNo, request.name)
            ofNiceDnr(result)
        } catch (e: ExternalApiFailException) {
            log.error("Nice DNR 조회 실패. carNo={}", request.carNo, e)

            // 예외를 던지지 않고 오류 정보만 context에 담아 return
            ofError(
                "NICE_DNR",
                create(
                    code = "NICE_DNR_ERROR",
                    message = e.message
                )
            )
        }
    }

    override fun toAdaptorRequest(
        request: LoanLimitRequest,
        externalDataContext: ExternalDataContext
    ): LoanLimitAdaptorRequest {
        val result = externalDataContext.niceDnrResult
        return LoanLimitAdaptorRequest(
            name = request.name,
            rrno = request.rrno,
            jobType = request.jobType,
            jobName = request.jobName,
            joinDate = request.joinDate,
            loanType = request.loanType,
            carNo = request.carNo,
            agreePersonalCreditInfo = request.agreePersonalCreditInfo,
            agreePersonalCreditTime = toDateTimeString(request.agreePersonalCreditTime),
            autoInfo = result?.autoInfo,
            autoSecondInfo = result?.autoSecondInfo
        )
    }

    override fun requiresExternalData(): Boolean = true

    override fun filterAvailablePartners(
        activePartnerCodes: List<PartnerCode>,
        context: ExternalDataContext
    ): List<PartnerCode> {
        // Nice DNR 실패 시 차량정보가 필요한 금융사 제외
        if (!context.hasNiceDnrError()) {
            log.warn("Nice DNR 조회 실패로 오토담보 한도조회 불가")
            return emptyList()
        }

        return activePartnerCodes
    }
}
