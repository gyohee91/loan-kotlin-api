package com.ghyinc.finance.domain.loan.entity

import com.ghyinc.finance.domain.loan.enums.LoanType
import com.ghyinc.finance.global.common.BaseTimeEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment

@Entity
class PartnerLoanType : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id")
    var partner: Partner? = null
        private set

    @Enumerated(EnumType.STRING)
    @Comment("대출 유형")
    var loanType: LoanType? = null

    @Column(nullable = false)
    @Comment("활성 여부")
    var active: Boolean? = null

    companion object {
        @JvmStatic
        fun create(
            partner: Partner? = null,
            loanType: LoanType,
            active: Boolean = false
        ): PartnerLoanType {
            val entity = PartnerLoanType()
            entity.partner = partner
            entity.loanType = loanType
            entity.active = active
            return entity
        }
    }
}
