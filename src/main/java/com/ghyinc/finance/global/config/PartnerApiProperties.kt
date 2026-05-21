package com.ghyinc.finance.global.config

import com.ghyinc.finance.domain.loan.enums.PartnerCode
import lombok.Getter
import lombok.Setter
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import java.util.*

@Component
@ConfigurationProperties(prefix = "loan-api")
class PartnerApiProperties {
    var partners: Map<PartnerCode, PartnerApiConfig> = emptyMap()

    fun getConfig(partnerCode: PartnerCode): PartnerApiConfig =
        partners[partnerCode]
            ?: throw IllegalArgumentException("금융사 API 설정이 없습니다: $partnerCode")

    class PartnerApiConfig {
        var baseUrl: String = ""
        var port = 0
        var path: String = ""
        var connectTimeoutMs = 3000 // 기본값 3초
        var readTimeoutMs = 5000 // 기본값 5초
    }
}
