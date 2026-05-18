package com.ghyinc.finance.domain.loan.dto

import com.ghyinc.finance.domain.external.coocon.dto.KbAppraisalResult
import com.ghyinc.finance.domain.external.nice.dto.NiceDnrResult
import lombok.Builder

/**
 * 외부 API 조회 결과 컨텍스트
 * @param niceDnrResult
 * @param kbAppraisalResult
 */
@JvmRecord
data class ExternalDataContext(
    val niceDnrResult: NiceDnrResult? = null,  // 오토담보 - Nice DNR
    val kbAppraisalResult: KbAppraisalResult? = null,  // 주담대   - KB부동산 시세
    val errors: MutableMap<String, ExternalDataError> = mutableMapOf() // 실패한 외부 조회 오류 정보
    // 상품 Type에 대한 외부 API 결과 데이터
) {
    fun hasNiceDnrError(): Boolean = errors.containsKey("NICE_DNR")

    fun hasKbAppraisalError(): Boolean = errors.containsKey("KB_APPRAISAL")

    companion object {
        // 빈 컨텍스트 (외부 조회 불필요한 대출 유형)
        @JvmStatic
        fun empty(): ExternalDataContext = ExternalDataContext()

        @JvmStatic
        fun ofNiceDnr(niceDnrResult: NiceDnrResult): ExternalDataContext =
            ExternalDataContext(niceDnrResult = niceDnrResult)

        @JvmStatic
        fun ofKbAppraisal(kbAppraisalResult: KbAppraisalResult): ExternalDataContext =
            ExternalDataContext(kbAppraisalResult = kbAppraisalResult)

        @JvmStatic
        fun ofError(key: String, error: ExternalDataError): ExternalDataContext =
            ExternalDataContext(errors = mutableMapOf(key to error))
    }
}
