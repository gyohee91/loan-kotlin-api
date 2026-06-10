package com.ghyinc.finance.global.exception

import com.ghyinc.finance.global.common.ErrorResponse
import com.ghyinc.finance.global.common.ErrorResponse.Companion.of
import org.apache.kafka.common.errors.InvalidRequestException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.context.request.async.AsyncRequestNotUsableException

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    /**
     * 암호화 처리 오류
     */
    @ExceptionHandler(CryptoException::class)
    fun handleCryptoException(e: CryptoException): ResponseEntity<ErrorResponse> {
        log.warn("CryptoException: {}", e.message)

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(of("암호화 처리 오류", e.message))
    }

    /**
     * 지원하지 않는 금융사 등 (비즈니스 요청 오류)
     */
    @ExceptionHandler(InvalidRequestException::class)
    fun handleInvalidRequestException(e: InvalidRequestException): ResponseEntity<ErrorResponse> {
        log.warn("InvalidRequestException: {}", e.message)

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(of(HttpStatus.BAD_REQUEST.reasonPhrase, e.message))
    }

    /**
     * 외부 API 5xx 에러
     */
    @ExceptionHandler(HttpServerErrorException::class)
    fun handleHttpServerErrorException(e: HttpServerErrorException): ResponseEntity<ErrorResponse> {
        log.error("HttpServerErrorException: {}", e.message, e)

        return ResponseEntity
            .status(HttpStatus.BAD_GATEWAY)
            .body(of(HttpStatus.BAD_GATEWAY.reasonPhrase, e.message))
    }

    /**
     * 외부 API 연결 실패
     */
    @ExceptionHandler(ResourceAccessException::class)
    fun handleResourceAccessException(e: ResourceAccessException): ResponseEntity<ErrorResponse> {
        log.error("ResourceAccessException: {}", e.message, e)

        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(of(HttpStatus.SERVICE_UNAVAILABLE.reasonPhrase, e.message))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        log.error("MethodArgumentNotValidException: {}", e.message, e)

        val errorMessage = e.bindingResult.fieldErrors.joinToString(", ") { error ->
            "${error.field}: ${error.defaultMessage}"
        }

        return ResponseEntity.badRequest()
            .body(of(HttpStatus.BAD_REQUEST.reasonPhrase, errorMessage))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<ErrorResponse> {
        if (e is AsyncRequestNotUsableException) {
            return ResponseEntity.noContent().build()
        }

        log.error("Exception: {}", e.message, e)

        return ResponseEntity.internalServerError()
            .body(of(HttpStatus.INTERNAL_SERVER_ERROR.reasonPhrase, e.message))
    }
}
