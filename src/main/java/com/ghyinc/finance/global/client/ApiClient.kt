package com.ghyinc.finance.global.client

import com.ghyinc.finance.domain.loan.enums.PartnerCode

/**
 * 통신 방식 추상화
 */
interface ApiClient {
    fun <T> post(partnerCode: PartnerCode, path: String, request: Any, responseType: Class<T>): T
}
