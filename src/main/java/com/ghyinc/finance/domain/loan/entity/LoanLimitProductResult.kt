package com.ghyinc.finance.domain.loan.entity

import com.ghyinc.finance.domain.loan.enums.LoanLimitResultCode
import com.ghyinc.finance.domain.loan.enums.PartnerCode
import com.ghyinc.finance.domain.loan.enums.PartnerInquiryStatus
import com.ghyinc.finance.global.common.BaseTimeEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment

@Entity
class LoanLimitProductResult : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    var loanLimitInquiry: LoanLimitInquiry? = null
        private set

    @Column(nullable = false, unique = true)
    @Comment("신청번호")
    var loReqtNo: String? = null

    @Enumerated(EnumType.STRING)
    @Comment("금융사 코드")
    var partnerCode: PartnerCode? = null

    @Comment("상품 코드")
    var productCode: String? = null

    @Enumerated(EnumType.STRING)
    @Comment("상품 처리상태")
    var status: PartnerInquiryStatus? = PartnerInquiryStatus.PENDING

    @Enumerated(EnumType.STRING)
    @Comment("한도조회 결과 코드")
    var resultCode: LoanLimitResultCode? = null

    @Comment("한도금액")
    var amount: Long? = null

    @Comment("금리")
    var interestRate = 0.0

    fun updateResult(resultCode: LoanLimitResultCode?, amount: Long, interestRate: Double) {
        this.status = PartnerInquiryStatus.SUCCESS
        this.resultCode = resultCode
        this.amount = amount
        this.interestRate = interestRate
    }

    fun assignInquiry(loanLimitInquiry: LoanLimitInquiry?) {
        this.loanLimitInquiry = loanLimitInquiry
    }

    fun sendSuccess() {
        this.status = PartnerInquiryStatus.SEND_SUCCESS
    }

    fun sendFail() {
        this.status = PartnerInquiryStatus.SEND_FAILED
    }

    companion object {
        @JvmStatic
        fun create(
            loanLimitInquiry: LoanLimitInquiry,
            loReqtNo: String,
            partnerCode: PartnerCode,
            productCode: String,
            status: PartnerInquiryStatus,
            resultCode: LoanLimitResultCode? = null,
            amount: Long,
            interestRate: Double
        ): LoanLimitProductResult {
            val entity = LoanLimitProductResult()
            entity.loanLimitInquiry = loanLimitInquiry
            entity.loReqtNo = loReqtNo
            entity.partnerCode = partnerCode
            entity.productCode = productCode
            entity.status = status
            entity.resultCode = resultCode
            entity.amount = amount
            entity.interestRate = interestRate
            return entity
        }
    }
}
