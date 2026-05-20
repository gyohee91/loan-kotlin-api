package com.ghyinc.finance.global.crypto

import com.ghyinc.finance.global.crypto.enums.CryptoAlgorithm

interface CryptoService {
    fun supports(algorithm: CryptoAlgorithm): Boolean
    fun encrypt(plainText: String): String
    fun decrypt(plainText: String): String
}
