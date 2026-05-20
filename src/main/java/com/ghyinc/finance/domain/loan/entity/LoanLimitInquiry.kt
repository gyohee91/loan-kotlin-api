package com.ghyinc.finance.domain.loan.entity

import com.ghyinc.finance.domain.loan.enums.InquiryStatus
import com.ghyinc.finance.domain.loan.enums.JobType
import com.ghyinc.finance.domain.loan.enums.LoanType
import com.ghyinc.finance.global.common.BaseTimeEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.time.LocalDateTime

@Entity
@Table(
    indexes = [
        Index(
            name = "idx_inquiry_user_id_loan_type_status",
            columnList = "user_id, loan_type, status"
        )
    ]
)
class LoanLimitInquiry: BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @Column(nullable = false, unique = true)
    @Comment("업무 식별번호")
    var inquiryNo: String? = null      // 외부 노출용 (FE 폴링, 콜백 연결, 이력 조회 KEY)

    @Column(nullable = false)
    @Comment("고객번호")
    var userId: Long? = null

    @Comment("고객명")
    var name: String? = null

    @Comment("CI")
    var ci: String? = null

    @Enumerated(EnumType.STRING)
    @Comment("직업 구분")
    var jobType: JobType? = null

    @Comment("직장명")
    var jobName: String? = null

    @Comment("입사년월(개업년월)")
    var joinDate: String? = null

    @Enumerated(EnumType.STRING)
    @Comment("대출 유형")
    var loanType: LoanType? = null

    @Comment("차량번호")
    var carNo: String? = null

    @Comment("신용정보 수집 이용 제공 동의 여부")
    var agreePersonalCreditInfo: Boolean = false

    @Comment("신용정보 수집 이용 제공 동의 시간")
    var agreePersonalCreditTime: LocalDateTime? = null

    @Enumerated(EnumType.STRING)
    @Comment("응답 결과")
    var status: InquiryStatus? = InquiryStatus.PENDING
        protected set

    @Comment("전체 상품 갯수")
    var totalProductCount: Int = 0
        protected set

    @Comment("Success 상품 갯수")
    var successProductCount: Int = 0
        protected set

    @OneToMany(mappedBy = "loanLimitInquiry", cascade = [CascadeType.ALL], orphanRemoval = true)
    val results: MutableList<LoanLimitResult> = mutableListOf()

    @OneToMany(mappedBy = "loanLimitInquiry", cascade = [CascadeType.ALL], orphanRemoval = true)
    val productResults: MutableList<LoanLimitProductResult> = mutableListOf()

    fun updateInquiryStatus(status: InquiryStatus) {
        this.status = status
    }

    fun addResult(result: LoanLimitResult) {
        this.results.add(result)
        result.assignInquiry(this)
    }

    fun addProductResult(productResult: LoanLimitProductResult) {
        this.productResults.add(productResult)
        productResult.assignInquiry(this)
    }

    fun initProductCount(total: Int) {
        this.totalProductCount = total
        this.successProductCount = 0
    }

    fun incrementSuccessCount() {
        this.successProductCount++
    }

    val progressRate: Int
        /**
         * 한도결과 수신 진행률 (0 ~ 100)
         * @return
         */
        get() {
            if (totalProductCount == 0) return 0
            return ((successProductCount * 100.0) / totalProductCount).toInt()
        }

    val isAllResultReceived: Boolean
        get() = successProductCount >= totalProductCount

    companion object {
        @JvmStatic
        fun create(
            inquiryNo: String,
            userId: Long,
            name: String? = null,
            ci: String? = null,
            loanType: LoanType? = null,
            jobType: JobType? = null,
            jobName: String? = null,
            joinDate: String? = null,
            carNo: String? = null,
            agreePersonalCreditInfo: Boolean = false,
            agreePersonalCreditTime: LocalDateTime? = null
        ): LoanLimitInquiry {
            val entity = LoanLimitInquiry()
            entity.inquiryNo = inquiryNo
            entity.userId = userId
            entity.name = name
            entity.ci = ci
            entity.loanType = loanType
            entity.jobType = jobType
            entity.jobName = jobName
            entity.joinDate = joinDate
            entity.carNo = carNo
            entity.agreePersonalCreditInfo = agreePersonalCreditInfo
            entity.agreePersonalCreditTime = agreePersonalCreditTime
            return entity
        }
    }
}
