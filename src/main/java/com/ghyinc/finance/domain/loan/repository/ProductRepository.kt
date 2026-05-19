package com.ghyinc.finance.domain.loan.repository

import com.ghyinc.finance.domain.loan.entity.Product
import com.ghyinc.finance.domain.loan.enums.LoanType
import com.ghyinc.finance.domain.loan.enums.PartnerCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProductRepository : JpaRepository<Product, Long> {
    @Query("""
        SELECT t 
        FROM Product t 
        WHERE t.partner.partnerCode = :partnerCode 
            AND t.loanType = :loanType 
            AND t.active = true
        """
    )
    fun findActiveByPartnerCodeAndLoanType(
        @Param("partnerCode") partnerCode: PartnerCode,
        @Param("loanType") loanType: LoanType
    ): MutableList<Product>
}
