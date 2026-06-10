package com.ghyinc.finance.global.exception

class ExternalApiFailException(
    val resultCode: String,
    message: String?
) : RuntimeException(message)
