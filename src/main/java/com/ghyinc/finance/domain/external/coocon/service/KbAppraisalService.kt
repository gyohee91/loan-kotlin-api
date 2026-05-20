package com.ghyinc.finance.domain.external.coocon.service

import com.ghyinc.finance.domain.external.coocon.dto.KbAppraisalResult
import com.ghyinc.finance.domain.external.coocon.dto.RespData
import org.springframework.stereotype.Service

@Service
class KbAppraisalService {
    fun inquireKbAppraisal(address: String?): KbAppraisalResult {
        // 로컬 환경 테스트를 위해 가 데이터 set
        val respData = RespData(
            region1Code = "11",
            region1Name = "서울시",
            region2Code = "11680",
            region2Name = "깅남구",
            region3Code = "11680108",
            region3Name = "논현동",
            ldongCode = "11680108"
        )

        // 쿠콘과 통신하여 KB부동산시세 결과 조회 후 리턴
        return KbAppraisalResult(
            respData = respData
        )
    }
}
