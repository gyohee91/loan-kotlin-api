package com.ghyinc.finance.domain.loan.enums

import com.fasterxml.jackson.annotation.JsonCreator

/**
 * 한도조회 결과 코드
 */
enum class LoanLimitResultCode(
    val code: String,
    val description: String
) {
    SUCCESS("00", "정상"),
    LIMIT_DENIED("11", "한도 부결"),
    DUPLICATE_REQUEST("21", "중복 신청"),
    INVALID_PRODUCT("22", "유효하지 않은 상품"),
    PARTNER_SYSTEM_ERROR("91", "금융사 시스템 오류"),
    TIMEOUT("92", "timeout"),
    UNKNOWN_ERROR("99", "알 수 없는 오류");

    val isSuccess: Boolean
        get() = this == SUCCESS

    companion object {
        @JsonCreator
        fun from(resultCode: String?): LoanLimitResultCode {
            for (code in entries) {
                if (code.code == resultCode) {
                    return code
                }
            }

            return UNKNOWN_ERROR
        }
    }
}
