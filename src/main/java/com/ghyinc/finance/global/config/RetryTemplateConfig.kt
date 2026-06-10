package com.ghyinc.finance.global.config

import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.retry.RetryCallback
import org.springframework.retry.RetryContext
import org.springframework.retry.RetryListener
import org.springframework.retry.RetryPolicy
import org.springframework.retry.backoff.ExponentialBackOffPolicy
import org.springframework.retry.policy.SimpleRetryPolicy
import org.springframework.retry.support.RetryTemplate
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import java.net.ConnectException
import java.net.SocketTimeoutException

@Configuration
class RetryTemplateConfig {
    private val log = LoggerFactory.getLogger(RetryTemplateConfig::class.java)

    companion object {
        private const val MAX_RETRY_ATTEMPTS = 3
    }

    @Bean
    fun retryTemplate(): RetryTemplate =
        RetryTemplate().apply {
            //Retry 정책: 어떤 예외를 몇 번 재시도할지
            setRetryPolicy(retryPolicy())

            //BackOff 정책: 재시도 간격
            setBackOffPolicy(backOffPolicy())

            //Retry 리스너 - 디버깅용
            registerListener(retryListener())
        }


    private fun retryPolicy(): RetryPolicy {
        val retryableExceptions = mapOf<Class<out Throwable>, Boolean>(
            //재시도 대상 예외
            ResourceAccessException::class.java to true, //네트워크 에러
            ConnectException::class.java to true,
            SocketTimeoutException::class.java to true,
            HttpServerErrorException::class.java to true //5xx 에러
        )

        //최대 3번 재시도(총 4번 시도: 최초 1번 + 재시도 3번)
        return SimpleRetryPolicy(MAX_RETRY_ATTEMPTS, retryableExceptions)
    }

    private fun backOffPolicy(): ExponentialBackOffPolicy =
        ExponentialBackOffPolicy().apply {
            //재시도 간격: 1초 -> 2초 -> 4초
            initialInterval = 1000  //첫 재시도: 1초 대기
            multiplier = 2.0        //다음 재시도: 2배씩 증가
            maxInterval = 10000     //최대 대기 기간: 10초
        }


    private fun retryListener(): RetryListener = object : RetryListener {
            override fun <T, E : Throwable> open(
                context: RetryContext,
                callback: RetryCallback<T, E>
            ): Boolean {
                //Retry 시작 시 (최초 1회만)
                log.debug("Retry 시작 - Retry 이름: {}", context.getAttribute("context.name"))
                return true
            }

            override fun <T, E : Throwable> close(
                context: RetryContext,
                callback: RetryCallback<T, E>,
                throwable: Throwable
            ) {
                //Retry 종료 시 (성공 또는 실패)
                if (throwable != null) {
                    log.error(
                        "Retry 최종 실패 - 총 시도: {}, 최종 에러: {}",
                        context.retryCount + 1,
                        throwable.message
                    )
                } else {
                    if (context.retryCount > 0) {
                        log.info("Retry 성공 - 총 시도: {}", context.retryCount + 1)
                    }
                }
            }

            override fun <T, E : Throwable> onError(
                context: RetryContext,
                callback: RetryCallback<T, E>,
                throwable: Throwable
            ) {
                //각 시도 실패 시마다 호출
                log.warn(
                    "Retry 에러 발생 - 시도: {}, 에러: {}",
                    context.retryCount + 1,
                    throwable.javaClass.getSimpleName()
                )
            }
        }

}
