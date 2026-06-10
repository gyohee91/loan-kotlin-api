package com.ghyinc.finance.global.config

import com.ghyinc.finance.domain.loan.enums.PartnerCode
import com.ghyinc.finance.global.client.LeaseLineConnection
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class LeaseLineConfig(
    private val partnerApiProperties: PartnerApiProperties
) {

    @Bean
    fun leaseLineConnections(): MutableMap<PartnerCode, LeaseLineConnection> =
        partnerApiProperties.partners.mapValues { (_, config) ->
            LeaseLineConnection(config.baseUrl, config.port)
        }.toMutableMap()

}
