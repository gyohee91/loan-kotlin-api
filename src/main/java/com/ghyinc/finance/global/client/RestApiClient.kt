package com.ghyinc.finance.global.client

import com.ghyinc.finance.domain.loan.enums.PartnerCode
import com.ghyinc.finance.global.exception.ExternalApiFailException
import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.Retry
import io.github.resilience4j.retry.RetryRegistry
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

/**
 * REST 방식
 */
@Component
class RestApiClient(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    private val retryRegistry: RetryRegistry,
    private val partnerRestClients: Map<PartnerCode, RestClient>
) : ApiClient {
    private val log = LoggerFactory.getLogger(RestApiClient::class.java)

    override fun <T> post(partnerCode: PartnerCode, path: String, request: Any, responseType: Class<T>): T {
        // 금융사별 독립 Circuit Breaker 적용
        val circuitBreaker = circuitBreakerRegistry.circuitBreaker(partnerCode.name)
        val retry = retryRegistry.retry(partnerCode.name)

        // Circuit Breaker 안에 Retry 적용
        // Retry -> Circuit Breaker 순으로 실행 (재시도가 모두 실패해야 Circuit Breaker 실패로 기록)
        return CircuitBreaker.decorateSupplier(circuitBreaker,
            Retry.decorateSupplier(retry) {
                log.info("[{}] Circuit Breaker 상태: {}", partnerCode, circuitBreaker.state)

                partnerRestClients[partnerCode]
                    ?.post()
                    ?.uri(path)
                    ?.header("X-Partner-Code", partnerCode.name)
                    ?.body(request)
                    ?.retrieve()
                    ?.onStatus(HttpStatusCode::is4xxClientError) { _, _ ->
                        throw ExternalApiFailException("한도조회_ERROR", "$partnerCode 4xx 오류")
                    }
                    ?.onStatus(HttpStatusCode::is5xxServerError) { _, _ ->
                        throw ExternalApiFailException("한도조회_ERROR", "$partnerCode 5xx 오류")
                    }
                    ?.body(responseType)
                    ?: throw ExternalApiFailException("한도조회_ERROR", "$partnerCode RestClient 없음")
            }
        ).get()
    }
}
