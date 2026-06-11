package com.ghyinc.finance.domain.notification.event

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.ghyinc.finance.domain.loan.enums.InquiryStatus
import com.ghyinc.finance.domain.notification.dto.NotificationSendRequest
import com.ghyinc.finance.domain.notification.enums.ChannelType
import com.ghyinc.finance.domain.notification.enums.SendType
import com.ghyinc.finance.domain.notification.service.NotificationService
import com.ghyinc.finance.global.event.LoanLimitCompletedEvent
import lombok.RequiredArgsConstructor
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Service

@Slf4j
@Service
@RequiredArgsConstructor
class LoanLimitCompletedEventConsumer(
    private val notificationService: NotificationService,
    private val objectMapper: ObjectMapper
) {
    private val log = LoggerFactory.getLogger(LoanLimitCompletedEventConsumer::class.java)

    companion object {
        private const val REQUEST_ID_KEY = "requestId"
    }

    @KafkaListener(
        topics = ["loan-limit-completed"],
        groupId = "notification-group"
    )
    fun consume(payload: String) {
        try {
            val event = objectMapper.readValue(payload, LoanLimitCompletedEvent::class.java)

            val requestId = event.requestId
            MDC.put(REQUEST_ID_KEY, requestId)

            log.info("[Consumer] 한도조회 완료 이벤트 수신. inquiryNo={}", event.inquiryNo)

            notificationService.sendNotification(
                NotificationSendRequest(
                    channelType = ChannelType.SMS,
                    sendType = SendType.IMMEDIATE,
                    recipient = event.name,
                    title = "한도조회 완료",
                    content = this.buildContent(event.status!!)
                )
            )
        } catch (e: JsonProcessingException) {
            throw RuntimeException(e)
        }
    }

    private fun buildContent(status: InquiryStatus): String =
        when (status) {
            InquiryStatus.SUCCESS -> "한도조회가 완료되었습니다. 결과를 확인해보세요"
            InquiryStatus.FAILED -> "한도조회 중 오류가 발생했습니다. 다시 시도해주세요."
            else -> "한도조회 상태가 업데이트되었습니다"
        }

}
