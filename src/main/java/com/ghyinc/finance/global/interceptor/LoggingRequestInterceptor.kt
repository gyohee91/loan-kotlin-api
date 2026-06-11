package com.ghyinc.finance.global.interceptor

import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpRequest
import org.springframework.http.HttpStatusCode
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class LoggingRequestInterceptor : ClientHttpRequestInterceptor {
    private val log = LoggerFactory.getLogger(LoggingRequestInterceptor::class.java)

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution
    ): ClientHttpResponse {
        //요청 로깅
        log.info(">>> URL: {}", request.uri)
        log.info(">>> Method: {}", request.method)
        log.info(">>> Headers: {}", request.headers)
        log.info(">>> Request Body: {}", String(body, StandardCharsets.UTF_8))

        val start = System.currentTimeMillis()
        val response = execution.execute(request, body)
        val duration = System.currentTimeMillis() - start

        val bufferedResponse: ClientHttpResponse = BufferingClientHttpResponseWrapper(response)

        //응답 로깅
        log.info("<<< Status Code: {}", bufferedResponse.statusCode)
        log.info("<<< Status Text: {}", bufferedResponse.statusText)
        log.info("<<< Headers: {}", bufferedResponse.headers)
        log.info(
            "<<< Body: {}",
            BufferedReader(InputStreamReader(bufferedResponse.body, StandardCharsets.UTF_8))
                .lines()
                .toList()
                .joinToString("")
        )
        log.info("<<< Duration: {}ms", duration)

        return bufferedResponse
    }

    /**
     * Body 재읽기 가능하도록 버퍼링
     */
    private class BufferingClientHttpResponseWrapper(
        private val response: ClientHttpResponse
    ) : ClientHttpResponse {

        private val body: ByteArray = response.body.readAllBytes()

        override fun getStatusCode(): HttpStatusCode = response.statusCode


        override fun getStatusText(): String = response.statusText


        override fun close() = response.close()


        override fun getBody(): InputStream = ByteArrayInputStream(body) // 매번 새 스트림 반환


        override fun getHeaders(): HttpHeaders = response.headers

    }
}
