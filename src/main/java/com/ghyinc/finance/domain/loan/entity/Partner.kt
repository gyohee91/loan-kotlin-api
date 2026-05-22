package com.ghyinc.finance.domain.loan.entity

import com.fasterxml.jackson.annotation.JsonIgnore
import com.ghyinc.finance.domain.loan.enums.PartnerCode
import com.ghyinc.finance.domain.loan.enums.PartnerType
import com.ghyinc.finance.global.common.BaseTimeEntity
import com.ghyinc.finance.global.crypto.enums.CryptoAlgorithm
import jakarta.persistence.*
import org.hibernate.annotations.Comment

@Entity
class Partner : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("제휴사 코드")
    var partnerCode: PartnerCode? = null

    @Column(nullable = false)
    @Comment("제휴사명")
    var partnerName: String? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Comment("제휴사 타입")
    var partnerType: PartnerType? = null

    @Column(nullable = false)
    @Comment("활성화 여부")
    var active = false

    @Enumerated(EnumType.STRING)
    @Comment("암호화 알고리즘")
    var algorithm: CryptoAlgorithm? = null

    @JsonIgnore
    @Comment("암호화키")
    var cryptoKey: String? = null

    @Column(length = 398)
    @Comment("공개키")
    var publicKey: String? = null

    @Column(length = 1624)
    @Comment("개인키")
    var privateKey: String? = null

    companion object {
        @JvmStatic
        fun create(
            partnerCode: PartnerCode,
            partnerName: String,
            partnerType: PartnerType,
            active: Boolean,
            algorithm: CryptoAlgorithm,
            cryptoKey: String,
            publicKey: String,
            privateKey: String
        ): Partner {
            val entity = Partner()
            entity.partnerCode = partnerCode
            entity.partnerName = partnerName
            entity.partnerType = partnerType
            entity.active = active
            entity.algorithm = algorithm
            entity.cryptoKey = cryptoKey
            entity.publicKey = publicKey
            entity.privateKey = privateKey
            return entity
        }
    }
}
