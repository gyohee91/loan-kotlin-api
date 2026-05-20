package com.ghyinc.finance.global.crypto

import com.ghyinc.finance.domain.loan.entity.Partner
import com.ghyinc.finance.domain.loan.enums.PartnerCode
import com.ghyinc.finance.domain.loan.repository.PartnerRepository
import com.ghyinc.finance.global.crypto.enums.CryptoAlgorithm
import com.ghyinc.finance.global.crypto.impl.AesCryptoService
import com.ghyinc.finance.global.crypto.impl.RsaCryptoService
import com.ghyinc.finance.global.exception.CryptoException
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Component
import java.util.*

@Component
class CryptoFactory(
    private val partnerRepository: PartnerRepository
) {
    private val log = LoggerFactory.getLogger(CryptoFactory::class.java)

    @Cacheable(value = ["cryptoService"], key = "#partnerCode")
    fun getCryptoService(partnerCode: PartnerCode): CryptoService {
        val partner = partnerRepository.findByPartnerCodeAndActive(partnerCode, true)
            .orElseThrow { CryptoException( "$partnerCode 파트너 정보 없음") }

        if (Objects.isNull(partner.algorithm)) {
            throw CryptoException("$partnerCode 암호화 설정 없음")
        }

        return this.buildCryptoService(partner)
    }

    // 키 교체 시 캐시 초기화 - 관리자 API 또는 Partner 업데이트 시 호출
    @CacheEvict(value = ["cryptoService"], key = "#partnerCode")
    fun evictCryptoService(partnerCode: PartnerCode?) {
        log.info("[{}] 암호화 설정 캐시 초기화", partnerCode)
    }

    // 전체 캐시 초기화
    @CacheEvict(value = ["cryptoService"], allEntries = true)
    fun evictAllCryptoService() {
        log.info("전체 암호화 설정 캐시 초기화")
    }

    private fun buildCryptoService(partner: Partner): CryptoService =
        when (partner.algorithm) {
            CryptoAlgorithm.AES_256_CBC,
            CryptoAlgorithm.AES_256_ECB -> AesCryptoService(partner.cryptoKey!!, partner.algorithm!!)
            CryptoAlgorithm.RSA_OAEP -> RsaCryptoService(partner.publicKey!!, partner.privateKey!!, partner.algorithm!!)
            else -> throw CryptoException("${partner.partnerCode} 지원하지 않는 암호화 알고리즘")
        }
}
