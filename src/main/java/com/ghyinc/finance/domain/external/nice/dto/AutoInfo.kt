package com.ghyinc.finance.domain.external.nice.dto

/**
 * 자동차등록원부(갑)
 * @param seq       갑/을부 행번호
 * @param formKind  갑/을부 구분
 * @param resCarNo  자동차등록번호
 * @param seatingCapacity   수용인원
 * @param resMotorType      원동기형식
 * @param resUseType        용도
 * @param resCarModelType   차종
 */
data class AutoInfo(
    val seq: String? = null,
    val formKind: String? = null,
    val resCarNo: String? = null,
    val seatingCapacity: String? = null,
    val resMotorType: String? = null,
    val resUseType: String? = null,
    val resCarModelType: String? = null //기타 등록원부 (갑)정보
) {
    companion object {
        @JvmStatic
        fun create(
            seq: String? = null,
            formKind: String? = null,
            resCarNo: String? = null,
            seatingCapacity: String? = null,
            resMotorType: String? = null,
            resUseType: String? = null,
            resCarModelType: String? = null
        ): AutoInfo = AutoInfo(
            seq = seq,
            formKind = formKind,
            resCarNo = resCarNo,
            seatingCapacity = seatingCapacity,
            resMotorType = resMotorType,
            resUseType = resUseType,
            resCarModelType = resCarModelType
        )
    }
}
