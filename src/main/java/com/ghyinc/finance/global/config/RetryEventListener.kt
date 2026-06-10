package com.ghyinc.finance.global.config

import io.github.resilience4j.retry.RetryRegistry
import jakarta.annotation.PostConstruct
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class RetryEventListener(
    private val retryRegistry: RetryRegistry
) {
    private val log = LoggerFactory.getLogger(RetryEventListener::class.java)

    @Value("\${resilience4j.retry.instances.default.max-attempts}")
    private lateinit var maxAttempts: String

    @PostConstruct
    fun registerListeners() {
        retryRegistry.retry("default")
            .eventPublisher
            .onRetry { event ->
                log.info(
                    "[Retry] 재시도 발생 - 시도 횟수{}/{}, 예외: {}",
                    event.numberOfRetryAttempts,
                    maxAttempts,
                    event.lastThrowable?.message
                )
            }
            .onError { event ->
                log.warn(
                    "[Retry] 재시도 소진 - 최종 실패. 예외: {}",
                    event.lastThrowable?.message
                )
            }
            .onSuccess { event ->
                log.debug(
                    "[Retry] 성공 - 총 시도 횟수: {}",
                    event.numberOfRetryAttempts
                )
            }
    }
}
