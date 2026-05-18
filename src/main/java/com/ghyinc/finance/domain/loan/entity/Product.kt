package com.ghyinc.finance.domain.loan.entity

import com.ghyinc.finance.domain.loan.enums.LoanType
import com.ghyinc.finance.global.common.BaseTimeEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment

@Entity
@Table(indexes = [Index(name = "idx_product_partner_code_loan_type", columnList = "partner_id, loan_type")])

class Product : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "partner_id", nullable = false)
    var partner: Partner? = null
        private set

    @Enumerated(EnumType.STRING)
    @Comment("대출유형")
    var loanType: LoanType? = null

    @Comment("상품코드")
    var productCode: String? = null

    @Comment("상품명")
    var productName: String? = null

    @Column(nullable = false)
    @Comment("활성화 여부")
    var active: Boolean? = null

    companion object {
        @JvmStatic
        fun create(
            partner: Partner? = null,
            loanType: LoanType,
            productCode: String,
            productName: String,
            active: Boolean
        ): Product {
            val entity = Product()
            entity.partner = partner
            entity.loanType = loanType
            entity.productCode = productCode
            entity.productName = productName
            entity.active = active
            return entity
        }
    }
}
