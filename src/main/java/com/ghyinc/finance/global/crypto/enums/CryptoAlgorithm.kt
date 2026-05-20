package com.ghyinc.finance.global.crypto.enums

enum class CryptoAlgorithm(
    val algorithm: String
) {
    AES_256_CBC("AES/CBC/PKCS5Padding"),
    AES_256_ECB("AES/ECB/PKCS5Padding"),
    RSA_OAEP("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
}
