package com.ghyinc.finance.domain.loan.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.ghyinc.finance.domain.loan.adaptor.dto.LoanLimitAdaptorRequest
import com.ghyinc.finance.domain.loan.adaptor.dto.LoanLimitAdaptorResponse.Companion.success
import com.ghyinc.finance.domain.loan.adaptor.impl.LoanLimitAdaptor
import com.ghyinc.finance.domain.loan.entity.LoanLimitInquiry
import com.ghyinc.finance.domain.loan.entity.LoanLimitInquiry.Companion.create
import com.ghyinc.finance.domain.loan.entity.Product
import com.ghyinc.finance.domain.loan.enums.InquiryStatus
import com.ghyinc.finance.domain.loan.enums.JobType
import com.ghyinc.finance.domain.loan.enums.LoanType
import com.ghyinc.finance.domain.loan.enums.PartnerCode
import com.ghyinc.finance.domain.loan.factory.LoanLimitAdaptorFactory
import com.ghyinc.finance.domain.loan.repository.LoanLimitInquiryRepository
import com.ghyinc.finance.domain.loan.repository.ProductRepository
import com.ghyinc.finance.global.common.DateUtils
import com.ghyinc.finance.global.common.LoReqtNoGenerator
import com.ghyinc.finance.global.exception.ExternalApiFailException
import com.ghyinc.finance.global.outbox.entity.OutboxEvent
import com.ghyinc.finance.global.outbox.entity.OutboxEvent.Companion.create
import com.ghyinc.finance.global.outbox.entity.OutboxStatus
import com.ghyinc.finance.global.outbox.event.OutboxCreatedEvent
import com.ghyinc.finance.global.outbox.repository.OutboxEventRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.slf4j.MDC
import org.springframework.context.ApplicationEventPublisher
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.Executor

@ExtendWith(MockitoExtension::class)
internal class LoanLimitSenderServiceTest {
    @InjectMocks
    private lateinit var loanLimitSenderService: LoanLimitSenderService

    @Mock
    private lateinit var loanLimitInquiryRepository: LoanLimitInquiryRepository

    @Mock
    private lateinit var productRepository: ProductRepository

    @Mock
    private lateinit var generator: LoReqtNoGenerator

    @Mock
    private lateinit var adaptorFactory: LoanLimitAdaptorFactory

    @Mock
    private lateinit var outboxEventRepository: OutboxEventRepository

    @Mock
    private lateinit var applicationEventPublisher: ApplicationEventPublisher

    @Mock
    private lateinit var objectMapper: ObjectMapper

    @Mock
    private lateinit var partnerApiExecutor: Executor

    @BeforeEach
    fun setUp() {
        val executor = ThreadPoolTaskExecutor().apply {
            corePoolSize = 4
            maxPoolSize = 4
            initialize()
        }
        ReflectionTestUtils.setField(
            loanLimitSenderService,
            "partnerApiExecutor",
            executor)
        MDC.put("requestId", "test-request-id")
    }

    @AfterEach
    fun tearDown() {
        MDC.clear()
    }

    private fun buildInquiry(): LoanLimitInquiry =
        create(
            inquiryNo = "",
            userId = 1L,
            name = "윤교희",
            ci = "",
            loanType = LoanType.PERSONAL_CREDIT,
            jobType = JobType.EMPLOYEE,
            jobName = "오케이",
            joinDate = null,
            carNo = null,
            agreePersonalCreditInfo = true,
            agreePersonalCreditTime = null
        )

    private fun buildProduct(productCode: String): Product =
        mock(Product::class.java).also {
                given(it.productCode).willReturn(productCode)
            }

    private fun buildAdaptorRequest(): LoanLimitAdaptorRequest =
        LoanLimitAdaptorRequest(
            name = "윤교희",
            rrno = "9102131234567",
            jobType = JobType.EMPLOYEE,
            jobName = "오케이",
            loanType = LoanType.PERSONAL_CREDIT,
            agreePersonalCreditInfo = true,
            agreePersonalCreditTime = DateUtils.toDateTimeString(LocalDateTime.now())
        )

    @Test
    @DisplayName("전송 성공 - LoanLimitResult SUCCESS, Inquiry SUCCESS")
    @Throws(JsonProcessingException::class)
    fun inquiry_sendSuccess() {
        // given
        val inquiry = this.buildInquiry()
        given(loanLimitInquiryRepository.findById(1L))
            .willReturn(Optional.of(inquiry))

        val product = this.buildProduct("P060100206")
        given(productRepository.findActiveByPartnerCodeAndLoanType(PartnerCode.LINE_BANK, LoanType.PERSONAL_CREDIT))
            .willReturn(listOf(product))
        given(generator.generate("LR")).willReturn("LR20260410AAA")

        val adaptor = mock(LoanLimitAdaptor::class.java)
        given(adaptorFactory.getAdaptor(PartnerCode.LINE_BANK)).willReturn(adaptor)
        given(adaptor.inquireLimit(eq(PartnerCode.LINE_BANK), any()))
            .willReturn(success(PartnerCode.LINE_BANK, 100L))

        given(objectMapper.writeValueAsString(any()))
            .willReturn("{\"inquiryNo\":\"LL20260410A3F2C891\"}")

        val savedOutboxEvent = create(
            "LoanLimitInquiry",
            "LL20260410A3F2C891",
            "LOAN_LIMIT_COMPLETED",
            "",
            OutboxStatus.PENDING,
            null,
            0
        ).also { ReflectionTestUtils.setField(it, "id", 1L) }

        given(outboxEventRepository.save(any<OutboxEvent>()))
            .willReturn(savedOutboxEvent)

        // when - 이벤트 객체로 직접 호출
        loanLimitSenderService.inquiry(1L, listOf(PartnerCode.LINE_BANK), this.buildAdaptorRequest())


        // then
        // Outbox INSERT 검증
        val outboxCaptor = argumentCaptor<OutboxEvent>()
        then(outboxEventRepository).should().save(outboxCaptor.capture())

        val capturedOutbox = outboxCaptor.firstValue
        assertThat(capturedOutbox.aggregateType).isEqualTo("LoanLimitInquiry")
        assertThat(capturedOutbox.eventType).isEqualTo("LOAN_LIMIT_COMPLETED")
        assertThat(capturedOutbox.status).isEqualTo(OutboxStatus.PENDING)

        // Spring 이벤트 발행 검증
        then(applicationEventPublisher).should().publishEvent(any<OutboxCreatedEvent>())

        assertThat(inquiry.status).isEqualTo(InquiryStatus.SUCCESS)
        assertThat(inquiry.results).hasSize(1)
        assertThat(inquiry.results[0].status).isEqualTo(InquiryStatus.SUCCESS)
    }

    @Test
    @DisplayName("전송 실패 - LoanLimitResult FAILED, Inquiry FAILED")
    fun inquiry_sendFailed_inquiryFailed() {
        val inquiry = this.buildInquiry()
        given(loanLimitInquiryRepository.findById(1L)).willReturn(Optional.of(inquiry))
        val product = this.buildProduct("P060100206")
        given(productRepository.findActiveByPartnerCodeAndLoanType(any(), any()))
            .willReturn(listOf(product))

        val adaptor = mock(LoanLimitAdaptor::class.java)
        given(adaptorFactory.getAdaptor(any())).willReturn(adaptor)
        given(adaptor.inquireLimit(any(), any()))
            .willThrow(ExternalApiFailException("한도조회_ERROR",  "${PartnerCode.LINE_BANK} 4xx 오류"))

        // when
        loanLimitSenderService.inquiry(1L, listOf(PartnerCode.LINE_BANK), this.buildAdaptorRequest())

        // then
        assertThat(inquiry.status).isEqualTo(InquiryStatus.FAILED)
        assertThat(inquiry.results[0].status).isEqualTo(InquiryStatus.FAILED)
    }

    @Test
    @DisplayName("상품별 신청번호 채번 후 ProductResult에 선저장 - 총 상품 수만큼 INSERT")
    fun inquiry_productResultPreSaved_withLoReqtNo() {
        // given
        val inquiry = this.buildInquiry()
        given(loanLimitInquiryRepository.findById(1L))
            .willReturn(Optional.of(inquiry))
        val product1 = this.buildProduct("P060100206")
        val product2 = this.buildProduct("P060100205")
        given(productRepository.findActiveByPartnerCodeAndLoanType(eq(PartnerCode.LINE_BANK), any()))
            .willReturn(listOf(product1, product2))
        given(generator.generate("LR")).willReturn("LR_AAA", "LR_BBB")

        val adaptor = mock(LoanLimitAdaptor::class.java)
        given(adaptorFactory.getAdaptor(any())).willReturn(adaptor)
        given(adaptor.inquireLimit(any(),any()))
            .willReturn(success(PartnerCode.LINE_BANK, 100L))

        // when
        loanLimitSenderService.inquiry(1L, listOf(PartnerCode.LINE_BANK), this.buildAdaptorRequest())

        // then
        assertThat(inquiry.productResults).hasSize(2)
        assertThat(inquiry.productResults[0].loReqtNo).isEqualTo("LR_AAA")
        assertThat(inquiry.productResults[1].loReqtNo).isEqualTo("LR_BBB")
        assertThat(inquiry.totalProductCount).isEqualTo(2)
    }
}