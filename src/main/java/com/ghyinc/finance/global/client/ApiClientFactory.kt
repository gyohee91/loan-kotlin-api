package com.ghyinc.finance.global.client

import com.ghyinc.finance.domain.loan.enums.PartnerCode
import com.ghyinc.finance.global.common.ConnectionType
import org.springframework.stereotype.Component

@Component
class ApiClientFactory(
    private val restApiClient: RestApiClient,
    private val leaseLineApiClient: LeaseLineApiClient
) {

    fun getApiClient(partnerCode: PartnerCode): ApiClient =
        when (partnerCode.connectionType) {
            ConnectionType.REST -> restApiClient
            ConnectionType.LEASE_LINE -> leaseLineApiClient
            ConnectionType.SOAP -> throw UnsupportedOperationException(
                "위 방식은 아직 지원하지 않습니다: $partnerCode"
            )
        }

}
