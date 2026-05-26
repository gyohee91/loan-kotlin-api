package com.ghyinc.finance.domain.loan.dto

data class RequestProduct(
    val loReqtNo: String? = null,
    val productCode: String
) {
    companion object {
        @JvmStatic
        fun create (
            loReqtNo: String,
            productCode: String
        ): RequestProduct =
            RequestProduct(
                loReqtNo = loReqtNo,
                productCode = productCode
            )
    }
}
