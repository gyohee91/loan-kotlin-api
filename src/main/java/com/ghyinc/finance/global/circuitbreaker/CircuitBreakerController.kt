package com.ghyinc.finance.global.circuitbreaker

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "Circuit Breaker 관리", description = "Circuit Breaker 상태 조회 및 제어")
@RestController
@RequestMapping("/api/admin/circuit-breakers")
class CircuitBreakerController(
    private val circuitBreakerStateService: CircuitBreakerStateService
) {

    @Operation(summary = "모든 Circuit Breaker 상태 조회")
    @GetMapping
    fun getAllCircuitBreakers(): ResponseEntity<Map<String, CircuitBreakerInfo>> =
        ResponseEntity.ok(circuitBreakerStateService.getAllCircuitBreakers())

    @Operation(summary = "Circuit Breaker 강제 OPEN")
    @PostMapping("/{name}/open")
    fun openCircuitBreaker(@PathVariable name: String): ResponseEntity<String> {
        circuitBreakerStateService.transitionToOpenState(name)
        return ResponseEntity.ok("Circuit Breaker OPEN: $name")
    }

    @Operation(summary = "Circuit Breaker 강제 CLOSE")
    @PostMapping("/{name}/close")
    fun closeCircuitBreaker(@PathVariable name: String): ResponseEntity<String> {
        circuitBreakerStateService.transitionToCloseState(name)
        return ResponseEntity.ok("Circuit Breaker CLOSE: $name")
    }

    @Operation(summary = "Circuit Breaker 강제 HALF_OPEN")
    @PostMapping("/{name}/half-open")
    fun halfOpenCircuitBreaker(@PathVariable name: String): ResponseEntity<String> {
        circuitBreakerStateService.transitionToHalfOpenState(name)
        return ResponseEntity.ok("Circuit Breaker HALF_OPEN: $name")
    }

    @Operation(summary = "Circuit Breaker 초기화")
    @PostMapping("/{name}/reset")
    fun resetCircuitBreaker(@PathVariable name: String): ResponseEntity<String> {
        circuitBreakerStateService.reset(name)
        return ResponseEntity.ok("Circuit Breaker 초기화: $name")
    }
}
