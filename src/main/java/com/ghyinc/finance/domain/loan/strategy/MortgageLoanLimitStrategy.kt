package com.ghyinc.finance.domain.loan.strategy

import com.ghyinc.finance.domain.external.coocon.service.KbAppraisalService
import com.ghyinc.finance.domain.loan.adaptor.dto.LoanLimitAdaptorRequest
import com.ghyinc.finance.domain.loan.dto.ExternalDataContext
import com.ghyinc.finance.domain.loan.dto.ExternalDataContext.Companion.ofError
import com.ghyinc.finance.domain.loan.dto.ExternalDataContext.Companion.ofKbAppraisal
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
class MortgageLoanLimitStrategy(
    private val kbAppraisalService: KbAppraisalService,
    private val partnerLoanTypeRepository: PartnerLoanTypeRepository
) : LoanLimitStrategy {
    private val log = LoggerFactory.getLogger(MortgageLoanLimitStrategy::class.java)

    override val loanType: LoanType = LoanType.MORTGAGE

    override val supportedBanks: List<PartnerCode>
        get() = partnerLoanTypeRepository.findActivePartnerCodeByLoanType(this.loanType)

    override fun validate(request: LoanLimitRequest) {
        // 법정동코드 필수 검증
        if (request.kbIdentityCode.isNullOrBlank()) {
            throw InvalidRequestException("주택담보대출은 법정동코드가 필수입니다")
        }
    }

    override fun fetchExternalData(request: LoanLimitRequest): ExternalDataContext {
        try {
            val result = kbAppraisalService.inquireKbAppraisal(request.address)
            return ofKbAppraisal(result)
        } catch (e: ExternalApiFailException) {
            log.error("KB 부동산 조회 실패. address={}", request.address, e)

            // 예외를 던지지 않고 오류 정보만 context에 담아 return
            return ofError(
                "KB_APPRAISAL",
                create(
                    code = "KB_APPRAISAL_ERROR",
                    message = e.message
                )
            )
        }
    }

    override fun toAdaptorRequest(
        request: LoanLimitRequest,
        externalDataContext: ExternalDataContext
    ): LoanLimitAdaptorRequest {
        val result = externalDataContext.kbAppraisalResult
        return LoanLimitAdaptorRequest(
            name = request.name,
            rrno = request.rrno,
            jobType = request.jobType,
            jobName = request.jobName,
            joinDate = request.joinDate,
            loanType = request.loanType,
            carNo = request.carNo,
            address = request.address,
            agreePersonalCreditInfo = request.agreePersonalCreditInfo,
            agreePersonalCreditTime = toDateTimeString(request.agreePersonalCreditTime),
            respData = result?.respData
        )
    }

    override fun requiresExternalData(): Boolean = true

    override fun filterAvailablePartners(
        activePartnerCodes: List<PartnerCode>,
        context: ExternalDataContext
    ): List<PartnerCode> {
        if (!context.hasKbAppraisalError()) {
            return activePartnerCodes // KB 시세 정상 -> 전체 금융사 진행
        }

        log.warn("KB부동산 시세 조회 실패. KB시세 불필요 금융사만 진행")

        // KB부동산 데이터가 없어도 자체 심사 가능한 금융사만 필터링
        return activePartnerCodes.filter { !requiresKbAppraisal(it) }
    }

    private fun requiresKbAppraisal(partnerCode: PartnerCode): Boolean =
        partnerCode in setOf(
            PartnerCode.KB_CAPITAL,
            PartnerCode.SHINHAN_BANK
        )
}
