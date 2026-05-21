package com.ghyinc.finance.global.common

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

internal class LoReqtNoGeneratorTest {
    private lateinit var generator: LoReqtNoGenerator

    @BeforeEach
    fun setUp() {
        generator = LoReqtNoGenerator()
    }

    @Test
    fun generate() {
        val loReqtNo = generator.generate("LL")
        println(loReqtNo)
    }

    @Test
    @DisplayName("동시 요청 1000건 - 중복없음")
    @Throws(InterruptedException::class)
    fun generate_noDuplicate() {
        val threadCount = 1000
        val executorService = Executors.newFixedThreadPool(32)
        val latch = CountDownLatch(threadCount)
        val results = ConcurrentHashMap.newKeySet<String>()

        repeat (threadCount) {
            executorService.submit {
                try {
                    results.add(generator.generate("LL"))
                } finally {
                    latch.countDown()
                }
            }
        }

        latch.await()
        executorService.shutdown()

        assertThat(results).hasSize(threadCount)
    }
}