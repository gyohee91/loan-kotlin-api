package com.ghyinc.finance.global.common

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * 날짜 시간 변환, 포맷팅
 */
object DateUtils {
    private val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
    private val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")

    /**
     * LocalDateTime -> yyyyMMddHHmmss 변환
     * @param date  LocalDateTime
     * @return      String (yyyyMMddHHmmss)
     */
    @JvmStatic
    fun toDateTimeString(date: LocalDateTime): String {
        return date.format(DATE_TIME_FORMATTER)
    }

    /**
     * LocalDateTime -> yyyyMMdd 변환
     * @param date  LocalDateTime
     * @return      String (yyyyMMdd)
     */
    @JvmStatic
    fun toDateString(date: LocalDateTime): String {
        return date.format(DATE_FORMATTER)
    }
}
