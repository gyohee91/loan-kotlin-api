package com.ghyinc.finance.global.config

import com.ghyinc.finance.global.interceptor.LoggingRequestInterceptor
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.client.BufferingClientHttpRequestFactory
import org.springframework.http.client.ClientHttpRequestFactory
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig {

    @Bean
    fun restTemplate(builder: RestTemplateBuilder): RestTemplate =
        builder
            .requestFactory(::bufferingClientHttpRequestFactory)
            .additionalInterceptors(LoggingRequestInterceptor()) //RetryInterceptor 제거 - Resilience4j로 대체
            //.additionalInterceptors(new RestTemplateRetryInterceptor(retryTemplate))
            .build()


    /**
     * 요청/응답 로깅을 위한 Buffering Factory
     */
    private fun bufferingClientHttpRequestFactory(): ClientHttpRequestFactory {
        val factory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(5000)
            setReadTimeout(10000)
        }
        return BufferingClientHttpRequestFactory(factory)
    }
}
