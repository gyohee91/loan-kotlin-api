package com.ghyinc.finance.domain.external.nice.dto

data class AutoSecondInfo(
    val seq: String? = null,
    val formKind: String? = null,
    val resEulNo: String? = null //기타 등록원부 (을)정보
) {
    companion object {
        @JvmStatic
        fun create(
            seq: String? = null,
            formKind: String? = null,
            resEulNo: String? = null
        ): AutoSecondInfo =
            AutoSecondInfo(
                seq = seq,
                formKind = formKind,
                resEulNo = resEulNo
            )
    }
}
