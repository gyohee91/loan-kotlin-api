package com.ghyinc.finance.global.common

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
class ApiCommResponse<T>(
    val success:Boolean = true,
    val message: String? = null,
    val data: T? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        @JvmStatic
        fun <T> success(message: String?, data: T?): ApiCommResponse<T> =
            ApiCommResponse(
                message = message,
                data = data
            )

        @JvmStatic
        fun <T> fail(message: String?): ApiCommResponse<T> =
            ApiCommResponse(
                success = false,
                message = message,
                data = null
            )
    }
}
