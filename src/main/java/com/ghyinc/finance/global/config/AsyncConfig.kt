package com.ghyinc.finance.global.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import java.util.concurrent.Executor
import java.util.concurrent.ThreadPoolExecutor

@EnableAsync
@Configuration
class AsyncConfig {
    /**
     * @Async("loanLimitExecutor") 전용
     * LoanLimitSenderService.inquiry() 처리
     * 한도조회 요청 1건 1스레드 점유
     */
    @Bean(name = ["loanLimitExecutor"])
    fun loanLimitExecutor(): Executor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = 10
            maxPoolSize = 30
            queueCapacity = 50
            threadNamePrefix = "loan-limit-"
            setWaitForTasksToCompleteOnShutdown(true)
            setAwaitTerminationSeconds(60)
            setTaskDecorator(MdcTaskDecorator())
            setRejectedExecutionHandler(ThreadPoolExecutor.CallerRunsPolicy())
            initialize()
        }


    /**
     * CompletableFuture.supplyAsync() 전용
     * 금융사별 API 병렬 전송
     */
    @Bean(name = ["partnerApiExecutor"])
    fun partnerApiExecutor(): Executor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = 50
            maxPoolSize = 150
            queueCapacity = 300
            threadNamePrefix = "partner-api-"
            setWaitForTasksToCompleteOnShutdown(true)
            setAwaitTerminationSeconds(30)
            setTaskDecorator(MdcTaskDecorator())
            setRejectedExecutionHandler(ThreadPoolExecutor.AbortPolicy())
            initialize()
        }
}
