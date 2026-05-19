package com.ghyinc.finance.domain.loan.repository

import com.ghyinc.finance.domain.loan.dto.LoanLimitProductResultDto
import com.ghyinc.finance.domain.loan.entity.LoanLimitInquiry
import com.ghyinc.finance.domain.loan.entity.LoanLimitProductResult
import jakarta.persistence.LockModeType
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface LoanLimitProductResultRepository : JpaRepository<LoanLimitProductResult, Long> {
    fun findByLoReqtNoAndProductCode(
        @Param("loReqtNo") loReqtNo: String,
        @Param("productCode") productCode: String
    ): Optional<LoanLimitProductResult>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t.loanLimitInquiry FROM LoanLimitProductResult t WHERE t.loReqtNo = :loReqtNo AND t.productCode = :productCode")
    fun findInquiryByLoReqtNoAndProduceCodeWithLock(
        @Param("loReqtNo") loReqtNo: String,
        @Param("productCode") productCode: String
    ): Optional<LoanLimitInquiry>

    @Query(
        """
            SELECT new com.ghyinc.finance.domain.loan.dto.LoanLimitProductResultDto(
               t.loReqtNo,
               t.partnerCode,
               t.productCode,
               t.resultCode,
               t.amount,
               t.interestRate
            )
            FROM LoanLimitProductResult t
            WHERE t.loanLimitInquiry.id = :inquiryId
            ORDER BY t.amount DESC NULLS LAST
            """
    )
    fun findProductResultsByInquiryId(
        @Param("inquiryId") inquiryId: Long,
        pageable: Pageable
    ): Page<LoanLimitProductResultDto>
}
