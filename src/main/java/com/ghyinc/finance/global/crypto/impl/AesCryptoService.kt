package com.ghyinc.finance.global.crypto.impl

import com.ghyinc.finance.global.crypto.CryptoService
import com.ghyinc.finance.global.crypto.enums.CryptoAlgorithm
import com.ghyinc.finance.global.exception.CryptoException
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AesCryptoService(
    key: String,
    private val algorithm: CryptoAlgorithm
) : CryptoService {
    private val log = LoggerFactory.getLogger(AesCryptoService::class.java)
    private val secretKeySpec: SecretKeySpec
    private val iv: ByteArray //CBC 모드용 초기화 벡터

    init {
        val keyBytes = Base64.getDecoder().decode(key)
        this.secretKeySpec = SecretKeySpec(keyBytes, "AES")
        this.iv = keyBytes.copyOf(16) //Key 앞 16byte를 IV로 사용
    }

    override fun supports(algorithm: CryptoAlgorithm): Boolean =
        algorithm == CryptoAlgorithm.AES_256_CBC ||
                algorithm == CryptoAlgorithm.AES_256_ECB

    override fun encrypt(plainText: String): String {
        try {
            val cipher = Cipher.getInstance(algorithm.algorithm)

            if (algorithm == CryptoAlgorithm.AES_256_CBC) {
                cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, IvParameterSpec(iv))
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec)
            }

            val encrypted = cipher.doFinal(plainText.toByteArray(StandardCharsets.UTF_8))
            return Base64.getEncoder().encodeToString(encrypted)
        } catch (e: Exception) {
            log.error("AES 암호화 오류", e)
            throw CryptoException(e.message)
        }
    }

    override fun decrypt(plainText: String): String {
        try {
            val cipher = Cipher.getInstance(algorithm.algorithm)

            if (algorithm == CryptoAlgorithm.AES_256_CBC) {
                cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, IvParameterSpec(iv))
            } else {
                cipher.init(Cipher.DECRYPT_MODE, secretKeySpec)
            }

            val decrypted = Base64.getDecoder().decode(plainText)
            return String(cipher.doFinal(decrypted), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            log.error("AES 복호화 오류", e)
            throw CryptoException(e.message)
        }
    }
}
