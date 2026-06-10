package com.ghyinc.finance.global.circuitbreaker

import io.github.resilience4j.circuitbreaker.CircuitBreaker

data class CircuitBreakerInfo(
    val name: String,
    val state: CircuitBreaker.State,
    val failureRate: Float,
    val slowCallRate: Float,
    val numberOfBufferedCalls: Int,
    val numberOfFailedCalls: Int,
    val numberOfSuccessfulCalls: Int,
    val numberOfSlowCalls: Int = 0,
    val numberOfNotPermittedCalls: Long
) {}
