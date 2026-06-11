package com.ghyinc.finance.global.interceptor

import org.slf4j.LoggerFactory
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.retry.support.RetryTemplate

class RestTemplateRetryInterceptor(
    private val retryTemplate: RetryTemplate
) : ClientHttpRequestInterceptor {
    private val log = LoggerFactory.getLogger(RestTemplateRetryInterceptor::class.java)

    companion object {
        private const val MAX_RETRY_ATTEMPTS = 3
    }

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse =
        retryTemplate.execute<ClientHttpResponse, Exception> { context ->
            if (context.retryCount > 0) {
                log.warn(
                    "재시도 중... {}번째 재시도 - URI: {}",
                    context.retryCount + 1,
                    request.uri
                )
            }
            try {
                execution.execute(request, body)
            } catch (e: Exception) {
                log.error(
                    "API 호출 실패- URI: {}, 시도: {}/{}",
                    request.uri,
                    context.retryCount + 1,
                    MAX_RETRY_ATTEMPTS,
                    e
                )

                throw e
            }
        }

}
