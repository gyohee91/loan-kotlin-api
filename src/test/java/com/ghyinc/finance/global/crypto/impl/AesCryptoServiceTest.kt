package com.ghyinc.finance.global.crypto.impl

import com.ghyinc.finance.global.crypto.enums.CryptoAlgorithm
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class AesCryptoServiceTest {
    private lateinit var aesCbcService: AesCryptoService
    private lateinit var aesEcbService: AesCryptoService

    companion object {
        // AES-256 테스트용 키 (32바이트 = 256bit)
        private val TEST_KEY: String =
            Base64.getEncoder().encodeToString("01234567890123456789012345678901".toByteArray())
    }

    @BeforeEach
    fun setUp() {
        aesCbcService = AesCryptoService(TEST_KEY, CryptoAlgorithm.AES_256_CBC)
        aesEcbService = AesCryptoService(TEST_KEY, CryptoAlgorithm.AES_256_ECB)
    }

    @Test
    @DisplayName("supports() CBC/ECB 시에만 true 반혼")
    fun supports() {
        assertThat(aesCbcService.supports(CryptoAlgorithm.AES_256_CBC)).isTrue()
        assertThat(aesCbcService.supports(CryptoAlgorithm.AES_256_ECB)).isTrue()
        assertThat(aesCbcService.supports(CryptoAlgorithm.RSA_OAEP)).isFalse()
    }

    @Test
    @DisplayName("AES-256-CBC: 암호화 후 복호화 시 원문 일치")
    fun aesCbc_encrypt() {
        val rrn = "9102131234557"

        val encrypted = aesCbcService.encrypt(rrn)
        val decrypted = aesCbcService.decrypt(encrypted)

        assertThat(encrypted).isNotEqualTo(rrn)
        assertThat(decrypted).isEqualTo(rrn)
    }

    @Test
    @DisplayName("AES-256-ECB: 암호화 후 복호화 시 원문 일치")
    fun aesEcb_encrypt() {
        val rrn = "9102131234567"

        val encrypted = aesEcbService.encrypt(rrn)
        val decrypted = aesEcbService.decrypt(encrypted)

        assertThat(encrypted).isNotEqualTo(rrn)
        assertThat(decrypted).isEqualTo(rrn)
    }

    @Test
    fun decrypt() {
    }
}