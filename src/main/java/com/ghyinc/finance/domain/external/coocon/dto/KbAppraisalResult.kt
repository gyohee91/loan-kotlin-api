package com.ghyinc.finance.domain.external.coocon.dto

data class KbAppraisalResult(
    val resultCd: String? = null,
    val resultMg: String? = null,
    val totalCount: String? = null,
    val respData: RespData? = null // KB부동산 시세 정보
) {
    companion object {
        @JvmStatic
        fun create(
            resultCd: String? = null,
            resultMg: String? = null,
            totalCount: String? = null,
            respData: RespData? = null
        ): KbAppraisalResult =
            KbAppraisalResult(
                resultCd = resultCd,
                resultMg = resultMg,
                totalCount = totalCount,
                respData = respData
            )
    }
}
