package com.ghyinc.finance.global.outbox.service

import com.ghyinc.finance.global.outbox.entity.OutboxEvent
import com.ghyinc.finance.global.outbox.entity.OutboxStatus
import com.ghyinc.finance.global.outbox.event.OutboxCreatedEvent
import com.ghyinc.finance.global.outbox.repository.OutboxEventRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.SendResult
import org.springframework.test.util.ReflectionTestUtils
import java.util.*
import java.util.concurrent.CompletableFuture

@ExtendWith(MockitoExtension::class)
class OutboxEventServiceTest {
    @InjectMocks
    private lateinit var outboxEventService: OutboxEventService

    @Mock
    private lateinit var outboxEventRepository: OutboxEventRepository

    @Mock
    private lateinit var kafkaTemplate: KafkaTemplate<String, String>

    private fun buildPendingOutboxEvent(): OutboxEvent =
        OutboxEvent().apply {
            aggregateType = "LoanLimitInquiry"
            aggregateId = "LL20260410A3F2C891"
            eventType = "LOAN_LIMIT_COMPLETED"
            payload = "{\"inquiryNo\":\"LL20260410A3F2C891\"}"
            status = OutboxStatus.PENDING
        }.also {
            ReflectionTestUtils.setField(it, "id", 1L)
        }

    @BeforeEach
    fun setUp() {
    }

    @Test
    @DisplayName("publishAfterCommit - OutboxEvent 조회 후 Kafka 발행")
    fun publishAfterCommit_fetchesOutboxAndPublishes() {
        // given
        val outboxEvent = this.buildPendingOutboxEvent()
        given(outboxEventRepository.findById(1L))
            .willReturn(Optional.of(outboxEvent))

        val future: CompletableFuture<SendResult<String, String>> =
            CompletableFuture.completedFuture(mock(SendResult::class.java) as SendResult<String, String>)

        given(kafkaTemplate.send(any(), any(), any())).willReturn(future)

        // when
        outboxEventService.publishAfterCommit(OutboxCreatedEvent(1L))

        then(kafkaTemplate).should().send(
            eq("loan-limit-completed"),
            eq("LL20260410A3F2C891"),
            any()
        )
    }

    @Test
    @DisplayName("publishToKafka 성공 - OutboxEvent PUBLISHED UPDATE")
    fun publishToKafka_success_markAsPublished() {
        // given
        val outboxEvent = this.buildPendingOutboxEvent()

        val future: CompletableFuture<SendResult<String, String>> =
            CompletableFuture.completedFuture(mock(SendResult::class.java) as SendResult<String, String>)
        given(kafkaTemplate.send(any(), any(), any())).willReturn(future)
        // when
        outboxEventService.publishToKafka(outboxEvent)

        assertThat(outboxEvent.status).isEqualTo(OutboxStatus.PUBLISHED)
        assertThat(outboxEvent.publishedAt).isNotNull()
    }

    @Test
    @DisplayName("publishAfterCommit 실패 - OutboxEvent PENDING 유지")
    fun publishAfterCommit_failure_keepPending() {
        // given
        val outboxEvent = this.buildPendingOutboxEvent()

        val failureFuture = CompletableFuture<SendResult<String, String>>()
        failureFuture.completeExceptionally(
            RuntimeException("Kafka 브로커 장애")
        )
        given(kafkaTemplate.send(any(), any(), any())).willReturn(failureFuture)        // whenComplete의 ex로 전달

        // when
        outboxEventService.publishToKafka(outboxEvent)

        // then - PENDING 유지 (배치가 재시도)
        assertThat(outboxEvent.status).isEqualTo(OutboxStatus.PENDING)
    }
}