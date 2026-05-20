package com.ghyinc.finance.domain.external.coocon.dto

data class RespData(
    val region1Code: String? = null,
    val region1Name: String? = null,
    val region2Code: String? = null,
    val region2Name: String? = null,
    val region3Code: String? = null,
    val region3Name: String? = null,
    val ldongCode: String? = null
) {
    companion object {
        @JvmStatic
        fun create(
            region1Code: String? = null,
            region1Name: String? = null,
            region2Code: String? = null,
            region2Name: String? = null,
            region3Code: String? = null,
            region3Name: String? = null,
            ldongCode: String? = null
        ): RespData =
            RespData(
                region1Code = region1Code,
                region1Name = region1Name,
                region2Code = region2Code,
                region2Name = region2Name,
                region3Code = region3Code,
                region3Name = region3Name,
                ldongCode = ldongCode
            )
    }
}
