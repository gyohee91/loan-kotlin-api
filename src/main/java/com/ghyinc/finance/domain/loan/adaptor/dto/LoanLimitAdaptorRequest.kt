package com.ghyinc.finance.domain.loan.adaptor.dto

import com.ghyinc.finance.domain.external.coocon.dto.RespData
import com.ghyinc.finance.domain.external.nice.dto.AutoInfo
import com.ghyinc.finance.domain.external.nice.dto.AutoSecondInfo
import com.ghyinc.finance.domain.loan.dto.RequestProduct
import com.ghyinc.finance.domain.loan.enums.JobType
import com.ghyinc.finance.domain.loan.enums.LoanType
import lombok.Builder

data class LoanLimitAdaptorRequest(
    val requestProducts: List<RequestProduct> = emptyList(),
    val name: String,
    val rrno: String,
    val jobType: JobType? = null,
    val jobName: String? = null,
    val joinDate: String? = null,
    val loanType: LoanType,
    val carNo: String? = null,
    val address: String? = null,
    val agreePersonalCreditInfo: Boolean,
    val agreePersonalCreditTime: String,
    val autoInfo: AutoInfo? = null,
    val autoSecondInfo: AutoSecondInfo? = null,
    val respData: RespData? = null
) {
    companion object {
        @JvmStatic
        fun create(
            name: String,
            rrno: String,
            jobType: JobType? = null,
            jobName: String? = null,
            joinDate: String? = null,
            loanType: LoanType,
            carNo: String? = null,
            address: String? = null,
            agreePersonalCreditInfo: Boolean,
            agreePersonalCreditTime: String,
            autoInfo: AutoInfo? = null,
            autoSecondInfo: AutoSecondInfo? = null,
            respData: RespData? = null
        ): LoanLimitAdaptorRequest =
            LoanLimitAdaptorRequest(
                name = name,
                rrno = rrno,
                jobType = jobType,
                jobName = jobName,
                joinDate = joinDate,
                loanType = loanType,
                carNo = carNo,
                address = address,
                agreePersonalCreditInfo = agreePersonalCreditInfo,
                agreePersonalCreditTime = agreePersonalCreditTime,
                autoInfo = autoInfo,
                autoSecondInfo = autoSecondInfo,
                respData = respData
            )
    }

    @JvmOverloads
    fun withRequestProducts(requestProducts: List<RequestProduct>): LoanLimitAdaptorRequest =
        this.copy(requestProducts = requestProducts)
}
