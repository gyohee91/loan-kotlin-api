package com.ghyinc.finance.domain.external.nice.dto

/**
 * Nice DNR 조회 결과 DTO
 * @param resultCode        결과 코드
 * @param autoInfo          갑구 정보
 * @param autoSecondInfo    을구 정보
 */
data class NiceDnrResult(
    val resultCode: String,
    val autoInfo: AutoInfo? = null,  //자동자등록원부(갑)
    val autoSecondInfo: AutoSecondInfo? = null //자동자등록원부(을)
) {
    companion object {
        @JvmStatic
        fun create(
            resultCode: String,
            autoInfo: AutoInfo? = null,
            autoSecondInfo: AutoSecondInfo? = null
        ): NiceDnrResult =
            NiceDnrResult(
                resultCode = resultCode,
                autoInfo = autoInfo,
                autoSecondInfo = autoSecondInfo
            )
    }
}
