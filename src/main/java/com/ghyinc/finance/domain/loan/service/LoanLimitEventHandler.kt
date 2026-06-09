package com.ghyinc.finance.domain.loan.service

import com.ghyinc.finance.global.event.LoanLimitInquiryCreatedEvent
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class LoanLimitEventHandler(
    private val loanLimitSenderService: LoanLimitSenderService
) {

    /**
     * 트랜잭션 커밋 후 이벤트 수신 -> inquiry() 호출
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("loanLimitExecutor")
    fun handleInquiryCreated(event: LoanLimitInquiryCreatedEvent) {
        loanLimitSenderService.inquiry(
            event.id,
            event.activePartnerCodes,
            event.adaptorRequest
        )
    }
}