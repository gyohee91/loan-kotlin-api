package com.ghyinc.finance.domain.user.entity

import com.ghyinc.finance.global.common.BaseTimeEntity
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.annotations.Comment

@Entity
class Member : BaseTimeEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var userId: Long? = null

    @Comment("고객명")
    var name: String? = null

    @Comment("휴대폰번호")
    var mobile: String? = null

    @Comment("이메일주소")
    var email: String? = null

    companion object {
        @JvmStatic
        fun create(
            name: String,
            mobile: String,
            email: String
        ): Member {
            val entity = Member()
            entity.name = name
            entity.mobile = mobile
            entity.email = email
            return entity
        }
    }
}
