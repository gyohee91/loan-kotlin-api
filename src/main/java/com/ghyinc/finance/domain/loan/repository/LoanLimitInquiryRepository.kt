package com.ghyinc.finance.domain.loan.repository

import com.ghyinc.finance.domain.loan.entity.LoanLimitInquiry
import com.ghyinc.finance.domain.loan.enums.InquiryStatus
import com.ghyinc.finance.domain.loan.enums.LoanType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface LoanLimitInquiryRepository : JpaRepository<LoanLimitInquiry, Long> {
    fun existsByUserIdAndLoanTypeAndStatus(
        @Param("userId") userId: Long,
        @Param("loanType") loanType: LoanType,
        @Param("status") status: InquiryStatus
    ): Boolean

    fun findByInquiryNo(@Param("inquiryNo") inquiryNo: String): Optional<LoanLimitInquiry>
}
