package com.ghyinc.finance.domain.external.nice.service

import com.ghyinc.finance.domain.external.nice.adaptor.NiceDnrAdaptor
import com.ghyinc.finance.domain.external.nice.dto.AutoInfo
import com.ghyinc.finance.domain.external.nice.dto.AutoSecondInfo
import com.ghyinc.finance.domain.external.nice.dto.NiceDnrRequest
import com.ghyinc.finance.domain.external.nice.dto.NiceDnrResult
import com.ghyinc.finance.global.exception.ExternalApiFailException
import org.springframework.stereotype.Service

/**
 * Nice DNR 조회 서비스 구현체
 */
@Service
class NiceDnrService(
    private val niceDnrAdaptor: NiceDnrAdaptor
) {

    /**
     * Nice DNR 조회
     *
     * @param carNo 차량번호
     * @param name  차주
     * @return      자동차등록원부 결과
     */
    fun inquireNiceDnr(carNo: String?, name: String?): NiceDnrResult {
        val request = NiceDnrRequest(
            ownerNm = name,
            vhrNo = carNo
        )

        val response = niceDnrAdaptor.inquire(request)

        if ("SUCCESS" != response.resultCode) {
            throw ExternalApiFailException("NICE_DNR_ERROR", "NICE DNR 오류 " + response.resultCode)
        }

        //NICE와 통신하여 DNR 결과 조회 후 리턴
        return this.toResponse(response)
    }

    private fun toResponse(response: NiceDnrResult): NiceDnrResult {
        // 로컬 환경 테스트를 위해 가 데이터 set
        val autoInfo = AutoInfo(
            seq = "1",
            formKind = "갑",
            resCarNo = "12가1234",
            seatingCapacity = "영업용",
            resMotorType = "5",
            resUseType = "64FP",
            resCarModelType = "승합대형"
        ) //갑 정보 가져옴

        val autoSecondInfo = AutoSecondInfo()

        return NiceDnrResult(
            resultCode = response.resultCode,
            autoInfo = autoInfo,
            autoSecondInfo = autoSecondInfo
        )
    }
}
