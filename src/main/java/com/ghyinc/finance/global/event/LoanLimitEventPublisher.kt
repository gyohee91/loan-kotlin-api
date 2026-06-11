package com.ghyinc.finance.global.event

interface LoanLimitEventPublisher {
    fun publishCompletedEvent(event: LoanLimitCompletedEvent)
}
