package com.ghyinc.finance.domain.external.nice.dto

/**
 * Nice DNR 조회 요청 DTO
 * @param apiKey
 * @param loginId
 * @param kindOf
 * @param ownerNm   차주명
 * @param vhrNo     차량번호
 */
data class NiceDnrRequest(
    val apiKey: String? = null,
    val loginId: String? = null,
    val kindOf: String? = null,
    val ownerNm: String? = null,
    val vhrNo: String? = null
) {
    companion object {
        @JvmStatic
        fun create(
            apiKey: String? = null,
            loginId: String? = null,
            kindOf: String? = null,
            ownerNm: String? = null,
            vhrNo: String? = null
        ): NiceDnrRequest =
            NiceDnrRequest(
                apiKey = apiKey,
                loginId = loginId,
                kindOf = kindOf,
                ownerNm = ownerNm,
                vhrNo = vhrNo
            )
    }
}
