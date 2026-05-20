package com.ghyinc.finance.global.common

import com.ghyinc.finance.global.common.DateUtils.toDateString
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

/**
 * 한도조회 상품별 신청번호 채번
 */
@Component
class LoReqtNoGenerator {
    fun generate(prefix: String): String {
        val date = toDateString(LocalDateTime.now())
        val uuid = UUID.randomUUID().toString()
            .replace("-".toRegex(), "")
            .substring(0, 8)
            .lowercase(Locale.getDefault())
        return prefix + date + uuid
    }
}
