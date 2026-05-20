package com.ghyinc.finance.global.crypto.impl

import com.ghyinc.finance.global.crypto.CryptoService
import com.ghyinc.finance.global.crypto.enums.CryptoAlgorithm
import com.ghyinc.finance.global.exception.CryptoException
import org.slf4j.LoggerFactory
import java.nio.charset.StandardCharsets
import java.security.KeyFactory
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*
import javax.crypto.Cipher

class RsaCryptoService(
    publicKeyStr: String,
    privateKeyStr: String,
    private val algorithm: CryptoAlgorithm
) : CryptoService {
    private val log = LoggerFactory.getLogger(RsaCryptoService::class.java)
    private val publicKey: PublicKey
    private val privateKey: PrivateKey

    init {
        try {
            val keyFactory = KeyFactory.getInstance("RSA")

            val publicKeyBytes = Base64.getDecoder().decode(publicKeyStr)
            this.publicKey = keyFactory.generatePublic(
                X509EncodedKeySpec(publicKeyBytes)
            )

            val privateKeyBytes = Base64.getDecoder().decode(privateKeyStr)
            this.privateKey = keyFactory.generatePrivate(
                PKCS8EncodedKeySpec(privateKeyBytes)
            )

        } catch (_: NoSuchAlgorithmException) {
            throw CryptoException("RSA 키 초기화 오류")
        } catch (_: InvalidKeySpecException) {
            throw CryptoException("RSA 키 초기화 오류")
        }
    }

    override fun supports(algorithm: CryptoAlgorithm): Boolean =
        algorithm == CryptoAlgorithm.RSA_OAEP


    override fun encrypt(plainText: String): String {
        try {
            val cipher = Cipher.getInstance(algorithm.algorithm)
            cipher.init(Cipher.ENCRYPT_MODE, publicKey)
            val encrypted = cipher.doFinal(
                plainText.toByteArray(StandardCharsets.UTF_8)
            )
            return Base64.getEncoder().encodeToString(encrypted)
        } catch (e: Exception) {
            log.error("RSA 암호화 오류", e)
            throw CryptoException("암호화 처리 중 오류 발생")
        }
    }

    override fun decrypt(plainText: String): String {
        try {
            val cipher = Cipher.getInstance(algorithm.algorithm)
            cipher.init(Cipher.DECRYPT_MODE, privateKey)
            val decrypted = Base64.getDecoder().decode(plainText)
            return String(cipher.doFinal(decrypted), StandardCharsets.UTF_8)
        } catch (e: Exception) {
            log.error("RSA 복호화 오류", e)
            throw CryptoException("복호화 처리 중 오류 발생")
        }
    }
}
