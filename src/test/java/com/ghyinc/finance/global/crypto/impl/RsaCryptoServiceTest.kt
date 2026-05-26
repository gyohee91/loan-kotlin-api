package com.ghyinc.finance.global.crypto.impl

import com.ghyinc.finance.global.crypto.enums.CryptoAlgorithm
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.spec.X509EncodedKeySpec
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

    @Test
    @DisplayName("키 검증")
    fun verifyKey() {
        val publicKeyStr = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAksA93OlyL+X08oCGVDRd36PhcC10ToB+xgcVfFbnXn21mc3hcxxVnLb509JBcwImRt3VHGTIzHiEUmzNKI3oaEP4dq3iHk6WAAy5gn76pXCPXbp+7yweRKhWLtGT9W+WVQvO7WMi6VLuQQmPvQ0+Au02/bmrnjauPS0qHftTiyjAhOy//LMAr861aXxq8PZK9zpRYiW/KHaPoPIhxChgVCBo9tdXTK7hCp0//ARaJ1WdC1M3bzRMBNmsKGphisa0eEQh1ArXoBA0tJtOKWNcVRuj8355u97a2dh2U8dFZ27E2phf97XLFlcOwtMNs4C1xXzF6kDdTJNeEn0dWSNhywIDAQAB"
        //val clean = publicKeyStr.replace("\n", "").replace(" ", "").trim()
        //println("length: ${clean.length}")

        val bytes = Base64.getDecoder().decode(publicKeyStr)
        val keyFactory = KeyFactory.getInstance("RSA")
        val key = keyFactory.generatePublic(X509EncodedKeySpec(bytes))
        println("key: $key")
    }

    @Test
    @DisplayName("키 생성")
    fun generateKey() {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()

        println("publicKey: ${Base64.getEncoder().encodeToString(keyPair.public.encoded)}")
        println("privateKey: ${Base64.getEncoder().encodeToString(keyPair.private.encoded)}")
    }
}