package com.ghyinc.finance.domain.loan.repository

import com.ghyinc.finance.domain.loan.entity.LoanLimitResult
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LoanLimitResultRepository : JpaRepository<LoanLimitResult, Long>
