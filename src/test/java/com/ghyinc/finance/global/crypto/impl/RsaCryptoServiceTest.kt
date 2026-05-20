package com.ghyinc.finance.global.crypto.impl

import com.ghyinc.finance.global.crypto.enums.CryptoAlgorithm
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.security.KeyPairGenerator
import java.util.*

@ExtendWith(MockitoExtension::class)
internal class RsaCryptoServiceTest {
    private lateinit var rsaCryptoService: RsaCryptoService

    @BeforeEach
    fun setUp() {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()

        val publicKey = Base64.getEncoder().encodeToString(keyPair.public.encoded)
        val privateKey = Base64.getEncoder().encodeToString(keyPair.private.encoded)

        rsaCryptoService = RsaCryptoService(publicKey, privateKey, CryptoAlgorithm.RSA_OAEP)
    }

    @Test
    @DisplayName("RSA supports()")
    fun supports() {
        assertThat(rsaCryptoService.supports(CryptoAlgorithm.RSA_OAEP)).isTrue()
        assertThat(rsaCryptoService.supports(CryptoAlgorithm.AES_256_CBC)).isFalse()
        assertThat(rsaCryptoService.supports(CryptoAlgorithm.AES_256_ECB)).isFalse()
    }

    @Test
    fun encrypt() {
        val plainText = "윤교희"

        val encData1 = rsaCryptoService.encrypt(plainText)
        val encData2 = rsaCryptoService.encrypt(plainText)

        // RSA OAEP는 랜덤 패딩이므로 매번 다른 암호문
        assertThat(encData1).isNotEqualTo(encData2)

        // 복호화 결과는 동일
        assertThat(rsaCryptoService.decrypt(encData1)).isEqualTo(plainText)
        assertThat(rsaCryptoService.decrypt(encData2)).isEqualTo(plainText)
    }

    @Test
    fun decrypt() {
    }
}