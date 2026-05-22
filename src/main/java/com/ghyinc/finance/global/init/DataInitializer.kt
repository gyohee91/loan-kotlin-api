package com.ghyinc.finance.global.init

import com.ghyinc.finance.domain.loan.entity.Partner
import com.ghyinc.finance.domain.loan.entity.PartnerLoanType
import com.ghyinc.finance.domain.loan.entity.Product
import com.ghyinc.finance.domain.loan.enums.LoanType
import com.ghyinc.finance.domain.loan.enums.PartnerCode
import com.ghyinc.finance.domain.loan.enums.PartnerType
import com.ghyinc.finance.domain.loan.repository.PartnerLoanTypeRepository
import com.ghyinc.finance.domain.loan.repository.PartnerRepository
import com.ghyinc.finance.domain.loan.repository.ProductRepository
import com.ghyinc.finance.domain.user.entity.Member
import com.ghyinc.finance.domain.user.repository.MemberRepository
import com.ghyinc.finance.global.crypto.enums.CryptoAlgorithm
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component

/**
 * 서버 기동 시 Partner 테이블 초기 데이터 Insert
 */
@Component
class DataInitializer(
    private val partnerRepository: PartnerRepository,
    private val partnerLoanTypeRepository: PartnerLoanTypeRepository,
    private val productRepository: ProductRepository,
    private val memberRepository: MemberRepository
) : ApplicationRunner {

    @Throws(Exception::class)
    override fun run(args: ApplicationArguments) {
        val initialPartner = listOf(
            Partner().apply {
                partnerCode = PartnerCode.KAKAO_BANK
                partnerName = PartnerCode.KAKAO_BANK.partnerName
                partnerType = PartnerType.BANK
                active = true
                algorithm = CryptoAlgorithm.AES_256_CBC
                cryptoKey = "wvtX75QJj1Uw1xKqw2kyPOVNBAmDr2vr"
            },
            Partner().apply {
                partnerCode = PartnerCode.TOSS_BANK
                partnerName = PartnerCode.TOSS_BANK.partnerName
                partnerType = PartnerType.BANK
                active = true
                algorithm = CryptoAlgorithm.AES_256_CBC
                cryptoKey = "bd0001eb9404dc257b90547d1343c4de"
            },
            Partner().apply {
                partnerCode = PartnerCode.KB_CAPITAL
                partnerName = PartnerCode.KB_CAPITAL.partnerName
                partnerType = PartnerType.CAPITAL
                active = true
                algorithm = CryptoAlgorithm.RSA_OAEP
                publicKey = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAksA93OlyL+X08oCGVDRd36PhcC10ToB+xgcVfFbnXn21mc3hcxxVnLb509JBcwImRt3VHGTIzHiEUmzNKI3oaEP4dq3iHk6WAAy5gn76pXCPXbp+7yweRKhWLtGT9W+WVQvO7WMi6VLuQQmPvQ0+Au02/bmrnjauPS0qHftTiyjAhOy//LMAr861aXxq8PZK9zpRYiW/KHaPoPIhxChgVCBo9tdXTK7hCp0//ARaJ1WdC1M3bzRMBNmsKGphisa0eEQh1ArXoBA0tJtOKWNcVRuj8355u97a2dh2U8dFZ27E2phf97XLFlcOwtMNs4C1xXzF6kDdTJNeEn0dWSNhywIDAQAB"
                privateKey = "MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQCSwD3c6XIv5fTygIZUNF3fo+FwLXROgH7GBxV8VudefbWZzeFzHFWctvnT0kFzAiZG3dUcZMjMeIRSbM0ojehoQ/h2reIeTpYADLmCfvqlcI9dun7vLB5EqFYu0ZP1b5ZVC87tYyLpUu5BCY+9DT4C7Tb9uaueNq49LSod+1OLKMCE7L/8swCvzrVpfGrw9kr3OlFiJb8odo+g8iHEKGBUIGj211dMruEKnT/8BFonVZ0LUzdvNEwE2awoamGKxrR4RCHUCtegEDS0m04pY1xVG6Pzfnm73trZ2HZTx0VnbsTamF/3tcsWVw7C0w2zgLXFfMXqQN1Mk14SfR1ZI2HLAgMBAAECggEANuLm4FqBpi+EOzTdyN2jJJN3LE0b3IaLo8Yff5dDRuCOjN1nTnXRAWGgM9zGNN0fcs7iN5HsVilXGDrwSpQ00dZmNgEX+szapl8b/RY/6aOdQ2mOsyaVlYk+XpltFNVs7RcTCE9VTd4CFVodpBAxWsfdEu2gJfb+mQvs3jTTTM7aoH/M7lUPtif7Nm4oMDiw+XBC1DBA+78vI7I8xHidxWwlNERh7eDrxnnqPoA7zkBIo1NKd3tiY3Z8Yz4r9/sqry/ZWayXVJAMYJhK1/AnbyTXsj27lG2nDUOt2d8KKE6ENMpeBOvkQYNUe0ftHQbZ4Cjds0DSenszX3xgg+vWbQKBgQDARXWeoXJ80881yz3GQXhU9Gn/0XL+DLIlcLlV519yZUzmhaKqs3ui4NP2XaH9MbgaBGQMW8QTXwXtHWNVwHNERmspCbMWYRgZXMV7vnS6+Q0ontzNiD+iXI29JKdnnghYsHfK3Z1DooxlcX5uHXR8a4Ur+qvkAfByC1m4QgonRwKBgQDDZE1oLSWO9uELgc0201ZdjV4ddHzpzxx2+83oJJAhyuZCs/KeXEVfRE1DCNn0fiJUj2xlGbIn9bBnsmYBsc1ERg/MMaank/rhXdVmTFdqxUBr+heypNqt0wOz02PbZDNMcqp06QfNEtKdV6fJE2Hce4rZPcwhu6piw8AeYsx7XQKBgE4Hom1nbhQ0zaIyQnbEOTaZHq+ga/+oXWAOeFjhln3RmLzxPQvz3VhD0CHq7APaerGsWIIfd9q4tKn/REIX4W+y+GhmNFT/wI0Cdm7641rCGlIC7u9GvTSCRU+eoYXOv+pma6db+yfovvuobLv5nj2kUR6BE+Nr2g4ehyHdQ0ufAoGALaqCoOd0UL6IiFz07mxkUgcZSP8Rtr+OIozlae0ptowqVsqh2LsuB376I8Gs1wSn12WR6usfhVFZwlKinqDEncFWLd4o2h+u0f9RQdBz2eNyFApmgX0gEuIvilbMjtkTWDmwdxSDmz1b9iQndcpO4+4H3JAh5nxq7RxdCX0D140CgYBLzEpO5AtTNiFnTvIxnwRpH0cu7zJ+Gh11gGFjFD6Pg96mBUt+CCsto6YcNzNSqczy+jD+VQ5VnTa2oYqO4R0nmLLeJTkjIiTUVo3WiP3npbjiW6wzyo0u0HwKZrO5iRnjoJQS/4NG6d0qPE2SKEs9X41MOEaJYbTuz9pMgFJymQ=="
            },
            Partner().apply {
                partnerCode = PartnerCode.K_BANK
                partnerName = PartnerCode.K_BANK.partnerName
                partnerType = PartnerType.BANK
                active = true
                algorithm = CryptoAlgorithm.AES_256_CBC
                cryptoKey = "AAEGRJuHwTWvrYsaa0V7vAqk+wZuSa2l"
            },
            Partner().apply {
                partnerCode = PartnerCode.LINE_BANK
                partnerName = PartnerCode.LINE_BANK.partnerName
                partnerType = PartnerType.BANK
                active = true
                algorithm = CryptoAlgorithm.AES_256_CBC
                cryptoKey = "AAEGRJuHwTWvrYsaa0V7vAqk+wZuSa2l"
            },
            Partner().apply {
                partnerCode = PartnerCode.SHINHAN_BANK
                partnerName = PartnerCode.SHINHAN_BANK.partnerName
                partnerType = PartnerType.BANK
                active = false
                algorithm = CryptoAlgorithm.AES_256_CBC
                cryptoKey = "AAEGRJuHwTWvrYsaa0V7vAqk+wZuSa2l"
            }
        )
        partnerRepository.saveAll(initialPartner)

        val initialPartnerLoanType = listOf(
            PartnerLoanType.create(
                partner = initialPartner.firstOrNull { it.partnerCode == PartnerCode.KAKAO_BANK },
                loanType = LoanType.PERSONAL_CREDIT,
                active = true
            ),
            PartnerLoanType.create(
                partner = initialPartner.firstOrNull { it.partnerCode == PartnerCode.TOSS_BANK },
                loanType = LoanType.PERSONAL_CREDIT,
                active = true
            ),
            PartnerLoanType.create(
                partner = initialPartner.firstOrNull { it.partnerCode == PartnerCode.LINE_BANK },
                loanType = LoanType.PERSONAL_CREDIT,
                active = true
            ),
            PartnerLoanType.create(
                partner = initialPartner.firstOrNull { it.partnerCode == PartnerCode.KB_CAPITAL },
                loanType = LoanType.BUSINESS,
                active = true
            ),
            PartnerLoanType.create(
                partner = initialPartner.firstOrNull { it.partnerCode == PartnerCode.SHINHAN_BANK },
                loanType = LoanType.PERSONAL_CREDIT,
                active = true
            )
        )

        partnerLoanTypeRepository.saveAll(initialPartnerLoanType)

        val initialProduct = listOf(
            Product.create(
                partner = initialPartner.firstOrNull { it.partnerCode == PartnerCode.LINE_BANK },
                loanType = LoanType.PERSONAL_CREDIT,
                productCode = "P060100206",
                productName = "사잇돌",
                active = true
            ),
            Product.create(
                partner = initialPartner.firstOrNull { it.partnerCode == PartnerCode.LINE_BANK },
                loanType = LoanType.PERSONAL_CREDIT,
                productCode = "P060100205",
                productName = "드림론",
                active = true
            ),
            Product.create(
                partner = initialPartner.firstOrNull { it.partnerCode == PartnerCode.KAKAO_BANK },
                loanType = LoanType.PERSONAL_CREDIT,
                productCode = "TA",
                productName = "갈아타기OK론",
                active = true
            ),
            Product.create(
                partner = initialPartner.firstOrNull { it.partnerCode == PartnerCode.TOSS_BANK },
                loanType = LoanType.PERSONAL_CREDIT,
                productCode = "FNQ005",
                productName = "kiwi비상금",
                active = true
            ),
            Product.create(
                partner = initialPartner.firstOrNull { it.partnerCode == PartnerCode.SHINHAN_BANK },
                loanType = LoanType.PERSONAL_CREDIT,
                productCode = "0201001074",
                productName = "비상금신한론",
                active = true
            )
        )
        productRepository.saveAll(initialProduct)

        val initialUser = listOf(
            Member().apply {
                name = "윤교희"
                mobile = "01056677055"
                email = "gyohee91@gmail.com"
            }
        )

        memberRepository.saveAll(initialUser)
    }
}
