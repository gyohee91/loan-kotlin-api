package com.ghyinc.finance.global.config

import com.ghyinc.finance.domain.loan.enums.PartnerCode
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import io.github.resilience4j.retry.RetryRegistry
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration

@Configuration
class CircuitBreakerEventConfig(
    private val circuitBreakerRegistry: CircuitBreakerRegistry,
    private val retryRegistry: RetryRegistry
) {
    private val log = LoggerFactory.getLogger(CircuitBreakerEventConfig::class.java)

    @PostConstruct
    fun registerEventListeners() {
        PartnerCode.entries.forEach { partnerCode ->
            val circuitBreaker = circuitBreakerRegistry.circuitBreaker(partnerCode.name)
            circuitBreaker.eventPublisher
                .onStateTransition { event ->
                    log.warn(
                        "[{}] Circuit Breaker 상태 변경: {} -> {}", partnerCode,
                        event.stateTransition.fromState,
                        event.stateTransition.toState
                    )
                }
                .onCallNotPermitted {
                    log.warn("[{}] Circuit Breaker OPEN - 호출 차단", partnerCode)
                }
                .onError {
                    log.error(
                        "[{}] Circuit Breaker 오류 감지. 실패율: {}%",
                        partnerCode, circuitBreaker.metrics.failureRate
                    )
                }
        }
    }

    @PostConstruct
    fun registerRetryEventListeners() {
        PartnerCode.entries.forEach { partnerCode ->
            val retry = retryRegistry.retry(partnerCode.name)
            retry.eventPublisher
                .onRetry { event ->
                    log.warn(
                        "[{}] Retry 시도. 횟수={}, 원인={}", partnerCode,
                        event.numberOfRetryAttempts,
                        event.lastThrowable?.message
                    )
                }
                .onError { event ->
                    log.error(
                        "[{}] Retry 모두 실패. 횟수={}",
                        partnerCode, event.numberOfRetryAttempts
                    )
                }
        }
    }
}
