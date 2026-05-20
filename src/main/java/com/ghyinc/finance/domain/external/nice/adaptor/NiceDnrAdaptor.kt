package com.ghyinc.finance.domain.external.nice.adaptor

import com.ghyinc.finance.domain.external.nice.config.NiceApiProperties
import com.ghyinc.finance.domain.external.nice.dto.NiceDnrRequest
import com.ghyinc.finance.domain.external.nice.dto.NiceDnrResult
import com.ghyinc.finance.global.exception.ExternalApiFailException
import lombok.extern.slf4j.Slf4j
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient

/**
 * Nice DNR API 통신 Adaptor
 *
 *
 * Nice API 전용 요청/응답 포맷 변환 담당
 * NiceDnrService -> NideDnrAdaptor -> Nice API
 */
@Slf4j
@Component
class NiceDnrAdaptor(
    @param:Qualifier("niceDnrRestClient") private val restClient: RestClient,
    private val niceApiProperties: NiceApiProperties
) {
    private val log = LoggerFactory.getLogger(NiceDnrAdaptor::class.java)

    /**
     * 자동차등록원부 단건 조회
     * @param request   조회 요청 (차량번호, 차주명 등)
     * @return          조회 결과
     */
    fun inquire(request: NiceDnrRequest): NiceDnrResult {
        val path = niceApiProperties.dnr.path

        return try {
            restClient.post()
                .uri(path)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError) {_, res ->
                    throw ExternalApiFailException("NICE_DNR_ERROR", "NICE DNR 4xx 오류 ${res.statusCode}")
                }
                .onStatus(HttpStatusCode::is5xxServerError) {_, res ->
                    throw ExternalApiFailException("NICE_DNR_ERROR", "NICE DNR 5xx 오류 ${res.statusCode}")
                }
                .body(NiceDnrResult::class.java)
        } catch (e: ExternalApiFailException) {
            throw e
        } catch (e: Exception) {
            log.error("Nice DNR 조회 오류. carNo={}", request.vhrNo, e)
            throw ExternalApiFailException("NICE_DNR_ERROR", "NICE DNR 오류 ${e.message}")
        }
    }
}
