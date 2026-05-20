package com.ghyinc.finance.domain.external.nice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * Nice DNR API 설정
 *
 * application.yml 설정 예시
 * <pre>
 * nice-api:
 * dnr:
 * base-url: https://api.nice.co.kr
 * path: /v1/vehicle/registration
 * api-key: nice-api-key
 * timeout-ms: 5000
</pre> *
 */
@Component
@ConfigurationProperties(prefix = "nice-api")
class NiceApiProperties {
    val dnr: NiceApiConfig = NiceApiConfig()

    class NiceApiConfig {
        val baseUrl: String? = null
        val path: String? = null
        val connectTimeoutMs = 0
        val readTimeoutMs = 0
    }
}
