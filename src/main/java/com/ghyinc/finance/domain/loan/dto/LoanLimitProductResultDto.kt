package com.ghyinc.finance.domain.loan.dto

import com.ghyinc.finance.domain.loan.enums.LoanLimitResultCode
import com.ghyinc.finance.domain.loan.enums.PartnerCode

data class LoanLimitProductResultDto(
    val loReqtNo: String?,
    val partnerCode: PartnerCode?,
    val productCode: String?,
    val resultCode: LoanLimitResultCode?,
    val amount: Long?,
    val interestRate: Double
)
