package com.ghyinc.finance.global.outbox.service

import com.ghyinc.finance.global.outbox.entity.OutboxEvent
import com.ghyinc.finance.global.outbox.event.OutboxCreatedEvent
import com.ghyinc.finance.global.outbox.repository.OutboxEventRepository
import lombok.extern.slf4j.Slf4j
import org.apache.kafka.common.errors.InvalidRequestException
import org.slf4j.LoggerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

/**
 * 트랜잭션 커밋 후 실행
 */
@Service
class OutboxEventService(
    private val outboxEventRepository: OutboxEventRepository,
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {
    private val log = LoggerFactory.getLogger(OutboxEventService::class.java)

    /**
     * 트랜잭션 커밋 후 즉시 Kafka 발행
     * @param event
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    fun publishAfterCommit(event: OutboxCreatedEvent) {
        outboxEventRepository.findById(event.id)
            .ifPresent { publishToKafka(it) }
    }

    fun publishToKafka(outboxEvent: OutboxEvent) {
        // aggregateType으로 Topic 분기 처리
        val topic = when (outboxEvent.aggregateType) {
            "LoanLimitInquiry" -> "loan-limit-completed"
            "Notification" -> "notification.send"
            else -> throw InvalidRequestException(
                "알 수 없는 aggregateType: ${outboxEvent.aggregateType}"
            )
        }

        try {
            kafkaTemplate.send(
                topic,
                outboxEvent.aggregateId,
                outboxEvent.payload
            )
                .whenComplete { result, ex ->
                    if (ex != null) {
                        log.error("Kafka 발행 실패", ex)
                        // 실패 시 PENDING 유지. 배치가 재시도
                    } else {
                        // 성공 시 PUBLISHED UPDATE
                        outboxEvent.markAsPublished()
                        outboxEventRepository.save(outboxEvent)
                        log.info(
                            "Kafka 발행 성공. partition={}",
                            result.recordMetadata.partition()
                        )
                    }
                }
        } catch (e: Exception) {
            // send() 자체 실패 (브로커 연결 불가 등)
            log.error("Kafka send() 실패. outboxId={}", outboxEvent.id, e)
            // PENDING 유지 -> 배치 재시도
        }
    }
}
