package com.ghyinc.finance.domain.loan.entity

import com.ghyinc.finance.domain.loan.enums.InquiryStatus
import com.ghyinc.finance.domain.loan.enums.PartnerCode
import com.ghyinc.finance.global.common.BaseTimeEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment

@Entity
class LoanLimitResult : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inquiry_id", nullable = false)
    var loanLimitInquiry: LoanLimitInquiry? = null
        protected set

    @Enumerated(EnumType.STRING)
    @Comment("금융사 코드")
    var partnerCode: PartnerCode? = null

    @Enumerated(EnumType.STRING)
    @Comment("응답 결과")
    var status: InquiryStatus? = null

    @Comment("실패 사유")
    var failReason: String? = null

    @Comment("응답 시간")
    var resTimeMs: Long? = null

    fun assignInquiry(loanLimitInquiry: LoanLimitInquiry?) {
        this.loanLimitInquiry = loanLimitInquiry
    }

    fun success(resTimeMs: Long) {
        this.status = InquiryStatus.SUCCESS
        this.resTimeMs = resTimeMs
    }

    fun fail(failReason: String?, resTimeMs: Long) {
        this.status = InquiryStatus.FAILED
        this.failReason = failReason
        this.resTimeMs = resTimeMs
    }

    companion object {
        @JvmStatic
        fun create(
            loanLimitInquiry: LoanLimitInquiry? = null,
            partnerCode: PartnerCode,
            status: InquiryStatus,
            failReason: String?,
            resTimeMs: Long
        ): LoanLimitResult {
            val entity = LoanLimitResult()
            entity.loanLimitInquiry = loanLimitInquiry
            entity.partnerCode = partnerCode
            entity.status = status
            entity.failReason = failReason
            entity.resTimeMs = resTimeMs
            return entity
        }
    }
}
