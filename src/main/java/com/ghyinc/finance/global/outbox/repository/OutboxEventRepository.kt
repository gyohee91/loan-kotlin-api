package com.ghyinc.finance.global.outbox.repository

import com.ghyinc.finance.global.outbox.entity.OutboxEvent
import com.ghyinc.finance.global.outbox.entity.OutboxStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface OutboxEventRepository : JpaRepository<OutboxEvent, Long> {
    /**
     * 재시도 대상 조회 (5분 이상 PENDING)
     */
    @Query(
        """
            SELECT t
            FROM OutboxEvent t
            WHERE t.status = :status
               AND t.createdAt < :threshold
            ORDER BY t.createdAt ASC
            LIMIT :limit
            """
    )
    fun findRetryTargets(
        @Param("status") status: OutboxStatus,
        @Param("threshold") threshold: LocalDateTime,
        @Param("limit") limit: Int
    ): MutableList<OutboxEvent>
}
