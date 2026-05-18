package com.ghyinc.finance.domain.loan.dto

/**
 * 외부 API 조회 에러 DTO
 */
@JvmRecord
data class ExternalDataError(
    val code: String? = null,
    val message: String? = null
){
    companion object {
        @JvmStatic
        fun create(
            code: String? = null,
            message: String? = null
        ): ExternalDataError = ExternalDataError(
            code = code,
            message = message
        )
    }
}
