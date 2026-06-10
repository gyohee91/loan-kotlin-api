package com.ghyinc.finance.global.config

import com.ghyinc.finance.domain.external.nice.config.NiceApiProperties
import com.ghyinc.finance.domain.loan.enums.PartnerCode
import com.ghyinc.finance.global.interceptor.LoggingRequestInterceptor
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.MediaType
import org.springframework.http.client.SimpleClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.util.Map

@Configuration
@EnableConfigurationProperties(PartnerApiProperties::class)
class RestClientConfig(
    private val partnerApiProperties: PartnerApiProperties,
    private val niceApiProperties: NiceApiProperties
) {

    /**
     * 금융사별 전용 RestClient Map
     *
     *
     * PartnerCode를 Key로 각 금융사의 baseUrl 등이 세팅된
     * RestClient를 미리 생성해두고 Adaptor에서 주입받아 사용
     * 금융사 추가 시 yml 설정만 추가하면 자동으로 RestClient가 생성됨.
     */
    @Bean
    fun partnerRestClients(): MutableMap<PartnerCode, RestClient> =
        partnerApiProperties.partners.mapValues { (_, config) ->
            this.buildRestClient(
                config.baseUrl,
                config.connectTimeoutMs,
                config.readTimeoutMs
            )
        }.toMutableMap()


    @Bean(name = ["niceDnrRestClient"])
    fun niceDnrRestClient(): RestClient {
        val config = niceApiProperties.dnr
        return this.buildRestClient(config.baseUrl, config.connectTimeoutMs, config.readTimeoutMs)
    }

    private fun buildRestClient(baseUrl: String, connectTimeoutMs: Int, readTimeoutMs: Int): RestClient {
        val factory = SimpleClientHttpRequestFactory().apply {
            setConnectTimeout(connectTimeoutMs)
            setReadTimeout(readTimeoutMs)
        }

        return RestClient.builder()
            .baseUrl(baseUrl)
            .requestFactory(factory)
            .defaultHeader("Content-Type", MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader("Accept", MediaType.APPLICATION_JSON_VALUE)
            .requestInterceptor(LoggingRequestInterceptor())
            .build()
    }
}
