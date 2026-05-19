package com.ghyinc.finance.domain.user.repository

import com.ghyinc.finance.domain.user.entity.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MemberRepository : JpaRepository<Member, Long>
