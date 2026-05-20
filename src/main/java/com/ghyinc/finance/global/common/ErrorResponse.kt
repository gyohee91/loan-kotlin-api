package com.ghyinc.finance.global.common

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
class ErrorResponse(
    val error: String? = null,
    val message: String? = null,
    val timestamp: LocalDateTime? = null
) {
    companion object {
        @JvmStatic
        fun of(
            error: String?,
            message: String?
        ): ErrorResponse =
            ErrorResponse(
                error = error,
                message = message,
                timestamp = LocalDateTime.now()
            )
    }
}
