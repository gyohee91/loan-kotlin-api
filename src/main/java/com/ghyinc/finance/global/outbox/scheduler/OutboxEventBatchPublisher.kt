package com.ghyinc.finance.global.outbox.scheduler

import com.ghyinc.finance.global.outbox.entity.OutboxStatus
import com.ghyinc.finance.global.outbox.repository.OutboxEventRepository
import com.ghyinc.finance.global.outbox.service.OutboxEventService
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class OutboxEventBatchPublisher(
    private val outboxEventService: OutboxEventService,
    private val outboxEventRepository: OutboxEventRepository
) {
    private val log = LoggerFactory.getLogger(OutboxEventBatchPublisher::class.java)

    /**
     * 1분마다 실행
     * 5분 이상 PENDING 상태인 건만 재시도
     * -> 즉시 발행 실패 or 비즈니스 트랜잭션 실패 건 처리
     */
    @Scheduled(fixedDelay = 60000)
    @SchedulerLock(name = "OutboxEventBatchPublisher_retryPendingEvents", lockAtLeastFor = "50s", lockAtMostFor = "55s")
    fun retryPendingEvents() {
        val retryTargets = outboxEventRepository.findRetryTargets(
            OutboxStatus.PENDING,
            LocalDateTime.now().minusMinutes(5),
            100
        )

        if (retryTargets.isEmpty()) return

        log.info("Outbox 재시도 대상: {} 건", retryTargets.size)

        retryTargets.forEach { outboxEvent ->
            try {
                outboxEventService.publishToKafka(outboxEvent)
                outboxEventRepository.save(outboxEvent)
            } catch (_: Exception) {
                outboxEvent.markAsFailed()
            }
        }
    }
}
