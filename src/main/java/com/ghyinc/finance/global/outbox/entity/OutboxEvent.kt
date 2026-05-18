package com.ghyinc.finance.global.outbox.entity

import com.ghyinc.finance.global.common.BaseTimeEntity
import jakarta.persistence.*
import org.hibernate.annotations.Comment
import java.time.LocalDateTime

@Entity
@Table(
    indexes = [
        Index(
            name = "idx_outbox_status_created_at",
            columnList = "status, created_at"
        )
    ]
)
class OutboxEvent : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    var aggregateType: String? = null

    @Column(nullable = false)
    var aggregateId: String? = null

    @Column(nullable = false)
    @Comment("이벤트 타입")
    var eventType: String? = null

    @Column(nullable = false, columnDefinition = "TEXT")
    @Comment("이벤트 payload (JSON)")
    var payload: String? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("발행 상태")
    var status: OutboxStatus? = null

    @Comment("발행 완료 시간")
    var publishedAt: LocalDateTime? = null

    @Comment("발행 실패 횟수")
    var failCount: Int = 0

    /**
     * Kafka 발행 성공
     */
    fun markAsPublished() {
        this.status = OutboxStatus.PUBLISHED
        this.publishedAt = LocalDateTime.now()
    }

    /**
     * Kafka 발행 실패
     */
    fun markAsFailed() {
        this.status = OutboxStatus.FAILED
        this.failCount++
    }

    companion object {
        @JvmStatic
        fun create(
            aggregateType: String,
            aggregateId: String,
            eventType: String,
            payload: String,
            status: OutboxStatus,
            publishedAt: LocalDateTime? = null,
            failCount: Int = 0
        ): OutboxEvent {
            val entity = OutboxEvent()
            entity.aggregateType = aggregateType
            entity.aggregateId = aggregateId
            entity.eventType = eventType
            entity.payload = payload
            entity.status = status
            entity.publishedAt = publishedAt
            entity.failCount = failCount
            return entity
        }
    }
}
