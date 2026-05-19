package com.ghyinc.finance.domain.loan.dto

import com.ghyinc.finance.domain.loan.entity.LoanLimitInquiry
import io.swagger.v3.oas.annotations.media.Schema
import org.springframework.data.domain.Page

@Schema(description = "금리 한도조회 (Polling)")
data class LoanLimitPollingResponse(
    @field:Schema(description = "업무 식별번호")
    val inquiryNo: String? = null,  // Polling 진행률 정보

    @field:Schema(description = "전체 상품 수")
    val totalProductCount: Int,

    @field:Schema(description = "한도결과 수신 완료 수")
    val successProductCount: Int,

    @field:Schema(description = "진행률")
    val progressRate: Int,

    @field:Schema(description = "전체 수신 완료 여부")
    val allResultReceived: Boolean,  // 페이징 정보

    @field:Schema(description = "한도 결과 목록")
    val productResults: List<LoanLimitProductResultDto?>,
    val currentPage: Int,
    val totalPages: Int,
    val totalElements: Long,
    val hasNext: Boolean
) {
    companion object {
        @JvmStatic
        fun from(
            inquiry: LoanLimitInquiry,
            productResults: Page<LoanLimitProductResultDto>
        ): LoanLimitPollingResponse =
            LoanLimitPollingResponse(
                inquiryNo = inquiry.inquiryNo ?: "",
                totalProductCount = inquiry.totalProductCount,
                successProductCount = inquiry.successProductCount,
                progressRate = inquiry.progressRate,
                allResultReceived = inquiry.isAllResultReceived,
                productResults = productResults.content,
                currentPage = productResults.number,
                totalPages = productResults.totalPages,
                totalElements = productResults.totalElements,
                hasNext = productResults.hasNext()
        )
    }
}
