package com.ghyinc.finance.domain.loan.repository

import com.ghyinc.finance.domain.loan.entity.Partner
import com.ghyinc.finance.domain.loan.enums.PartnerCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface PartnerRepository : JpaRepository<Partner, Long> {
    fun findByPartnerCodeAndActive(
        @Param("partnerCode") partnerCode: PartnerCode,
        active: Boolean
    ): Optional<Partner>
}
