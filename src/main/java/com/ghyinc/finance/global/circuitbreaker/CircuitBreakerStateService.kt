package com.ghyinc.finance.global.circuitbreaker

import io.github.resilience4j.circuitbreaker.CircuitBreaker
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CircuitBreakerStateService(
    private val circuitBreakerRegistry: CircuitBreakerRegistry
) {
    private val log = LoggerFactory.getLogger(CircuitBreakerStateService::class.java)

    /**
     * Circuit Breaker 상태 조회
     */
    fun getState(name: String): CircuitBreaker.State =
        circuitBreakerRegistry.circuitBreaker(name).state


    /**
     * Circuit Breaker 상태 정보 조회
     */
    fun getCircuitBreakerInfo(name: String): CircuitBreakerInfo {
        val circuitBreaker = circuitBreakerRegistry.circuitBreaker(name)
        val metrics = circuitBreaker.metrics

        return CircuitBreakerInfo(
            name = name,
            state = circuitBreaker.state,
            failureRate = metrics.failureRate, //실패 호출 비율(%). failure-rate-threshold: 50 설정 시 이 값이 50% 이상이면 CLOSED → OPEN 전환
            slowCallRate = metrics.slowCallRate, //느린 호출 비율(%). slow-call-duration-threshold 초과 호출의 비율이며, slow-call-rate-threshold: 50 설정 시 50% 이상이면 OPEN 전환
            numberOfBufferedCalls = metrics.numberOfBufferedCalls, //전체 호출 수. numberOfSuccessfulCalls + numberOfFailedCalls의 합계
            numberOfFailedCalls = metrics.numberOfFailedCalls, //record-exceptions에 해당하는 예외가 발생한 호출이 카운트됨
            numberOfSuccessfulCalls = metrics.numberOfSuccessfulCalls, //성공으로 기록된 호출 수
            numberOfNotPermittedCalls = metrics.numberOfNotPermittedCalls //OPEN 상태에서 차단된 호출 수
        )
    }

    /**
     * 모든 Circuit Breaker 상태 조회
     */
    fun getAllCircuitBreakers(): Map<String, CircuitBreakerInfo> =
        circuitBreakerRegistry.allCircuitBreakers
            .associateBy(
                { it.name },
                { this.getCircuitBreakerInfo(it.name) }
            )

    /**
     * Circuit Breaker 강제 OPEN
     */
    fun transitionToOpenState(name: String) {
        circuitBreakerRegistry.circuitBreaker(name).transitionToOpenState()
        log.warn("Circuit Breaker 강제 OPEN: {}", name)
    }

    /**
     * Circuit Breaker 강제 CLOSE
     */
    fun transitionToCloseState(name: String) {
        circuitBreakerRegistry.circuitBreaker(name).transitionToClosedState()
        log.info("Circuit Breaker 강제 CLOSE: {}", name)
    }

    /**
     * Circuit Breaker 강제 HALF_OPEN
     */
    fun transitionToHalfOpenState(name: String) {
        circuitBreakerRegistry.circuitBreaker(name).transitionToHalfOpenState()
        log.warn("Circuit Breaker 강제 HALF_OPEN: {}", name)
    }

    /**
     * Circuit Breaker 메트릭 초기화
     */
    fun reset(name: String) {
        circuitBreakerRegistry.circuitBreaker(name).reset()
        log.info("Circuit Breaker 초기화: {}", name)
    }
}
