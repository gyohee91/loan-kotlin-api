package com.ghyinc.finance.domain.loan.service

import com.ghyinc.finance.domain.loan.adaptor.dto.LoanLimitAdaptorRequest
import com.ghyinc.finance.domain.loan.dto.ExternalDataContext.Companion.empty
import com.ghyinc.finance.domain.loan.dto.ExternalDataContext.Companion.ofError
import com.ghyinc.finance.domain.loan.dto.ExternalDataError.Companion.create
import com.ghyinc.finance.domain.loan.dto.LoanLimitRequest.Companion.create
import com.ghyinc.finance.domain.loan.entity.LoanLimitInquiry
import com.ghyinc.finance.domain.loan.enums.InquiryStatus
import com.ghyinc.finance.domain.loan.enums.JobType
import com.ghyinc.finance.domain.loan.enums.LoanType
import com.ghyinc.finance.domain.loan.enums.PartnerCode
import com.ghyinc.finance.domain.loan.factory.LoanLimitStrategyFactory
import com.ghyinc.finance.domain.loan.repository.LoanLimitInquiryRepository
import com.ghyinc.finance.domain.loan.strategy.LoanLimitStrategy
import com.ghyinc.finance.global.common.LoReqtNoGenerator
import com.ghyinc.finance.global.event.LoanLimitInquiryCreatedEvent
import org.apache.kafka.common.errors.InvalidRequestException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.util.ReflectionTestUtils
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
internal class LoanLimitServiceTest {
    @InjectMocks
    private lateinit var loanLimitService: LoanLimitService

    @Mock
    private lateinit var strategyFactory: LoanLimitStrategyFactory

    @Mock
    private lateinit var generator: LoReqtNoGenerator

    @Mock
    private lateinit var loanLimitInquiryRepository: LoanLimitInquiryRepository

    @Mock
    private lateinit var applicationEventPublisher: ApplicationEventPublisher

    @Test
    @DisplayName("한도조회 요청 정상처리 - 202 Accepted 즉시 응답")
    fun requestCompareLoan_success() {
        // given
        val request = create(
            1L,
            "윤교희",
            "9102131234556",
            "wEi9oYSuekQGxT9MV4rKHG4CO+Zrp+onhLIIuembI8jx/0PLF5Ne3oMBxvUFlN4UmsgjeNErZfmpCVUFH",
            JobType.EMPLOYEE,
            "오케이",
            LoanType.PERSONAL_CREDIT,
            true,
            LocalDateTime.now()
        )

        val strategy = mock(LoanLimitStrategy::class.java)
        given(strategyFactory.getStrategy(LoanType.PERSONAL_CREDIT))
            .willReturn(strategy)
        given(strategy.requiresExternalData()).willReturn(false)
        given(strategy.supportedBanks)
            .willReturn(listOf(PartnerCode.KAKAO_BANK, PartnerCode.TOSS_BANK))
        given(strategy.filterAvailablePartners(any(), any()))
            .willReturn(listOf(PartnerCode.KAKAO_BANK, PartnerCode.TOSS_BANK))
        given(generator.generate("LL")).willReturn("LL20260416ANWOW")
        given(strategy.toAdaptorRequest(any(), any()))
            .willReturn(mock(LoanLimitAdaptorRequest::class.java))

        given(loanLimitInquiryRepository.save(any<LoanLimitInquiry>()))
            .willAnswer { invocation ->
                val inquiry = invocation.getArgument<LoanLimitInquiry>(0)
                ReflectionTestUtils.setField(inquiry, "id", 1L)
                inquiry
            }

        // when
        val response = loanLimitService.requestCompareLoan(request)

        // then
        assertThat(response.success).isEqualTo(true)
        then(loanLimitInquiryRepository).should().save(any<LoanLimitInquiry>())

        // 이벤트 발행 검증
        val eventCaptor = argumentCaptor<LoanLimitInquiryCreatedEvent>()
        then(applicationEventPublisher).should().publishEvent(eventCaptor.capture())
        assertThat(eventCaptor.firstValue.id).isEqualTo(1L)
        assertThat(eventCaptor.firstValue.activePartnerCodes)
            .containsExactly(PartnerCode.KAKAO_BANK, PartnerCode.TOSS_BANK)
    }

    @Test
    @DisplayName("활성화된 금융사가 없으면 InvalidRequestException 발생")
    fun requestCompareLoan_noActivePartner_throwException() {
        // given
        val request = create(
            1L,
            "윤교희",
            "9102131234556",
            "wEi9oYSuekQGxT9MV4rKHG4CO+Zrp+onhLIIuembI8jx/0PLF5Ne3oMBxvUFlN4UmsgjeNErZfmpCVUFH",
            JobType.EMPLOYEE,
            "오케이",
            LoanType.PERSONAL_CREDIT,
            true,
            LocalDateTime.now()
        )
        val strategy = mock(LoanLimitStrategy::class.java)
        given(strategyFactory.getStrategy(LoanType.PERSONAL_CREDIT))
            .willReturn(strategy)
        given(strategy.requiresExternalData()).willReturn(false)
        given(strategy.supportedBanks).willReturn(emptyList())

        // when & then
        assertThatThrownBy { loanLimitService.requestCompareLoan(request) }
            .isInstanceOf(InvalidRequestException::class.java)
            .hasMessage("현재 조회 가능한 금융사가 없습니다")

        // 이벤트 미발행 검증
        then(applicationEventPublisher).should(never()).publishEvent(any<LoanLimitInquiryCreatedEvent>())
    }

    @Test
    @DisplayName("진행 중인 한도조회 요청이 있으면 중복 요청 방지")
    fun requestCompareLoan_inProgressExists_throwsException() {
        // given
        val request = create(
            1L,
            "윤교희",
            "9102131234556",
            "wEi9oYSuekQGxT9MV4rKHG4CO+Zrp+onhLIIuembI8jx/0PLF5Ne3oMBxvUFlN4UmsgjeNErZfmpCVUFH",
            JobType.EMPLOYEE,
            "오케이",
            LoanType.PERSONAL_CREDIT,
            true,
            LocalDateTime.now()
        )

        given(loanLimitInquiryRepository.existsByUserIdAndLoanTypeAndStatus(
                1L, LoanType.PERSONAL_CREDIT, InquiryStatus.IN_PROGRESS
            )).willReturn(true)

        // when & then
        assertThatThrownBy { loanLimitService.requestCompareLoan(request) }
            .isInstanceOf(InvalidRequestException::class.java)
            .hasMessage("진행 중인 한도조회가 있습니다.")

        then(loanLimitInquiryRepository).should(never()).save(any())
        then(applicationEventPublisher).should(never()).publishEvent(any<LoanLimitInquiryCreatedEvent>())
    }

    @Test
    @DisplayName("오토담보 - Nice DNR 조회 성공 시 정상 처리")
    fun requestCompareLoan_auto_niceDnrSuccess() {
        // given
        val request = create(
            1L,
            "윤교희",
            "9102131234556",
            "wEi9oYSuekQGxT9MV4rKHG4CO+Zrp+onhLIIuembI8jx/0PLF5Ne3oMBxvUFlN4UmsgjeNErZfmpCVUFH",
            JobType.EMPLOYEE,
            "오케이",
            LoanType.AUTO,
            true,
            LocalDateTime.now()
        )
        val strategy = mock(LoanLimitStrategy::class.java)
        given(strategyFactory.getStrategy(any())).willReturn(strategy)
        given(strategy.requiresExternalData()).willReturn(true)
        given(strategy.fetchExternalData(any())).willReturn(empty())
        given(strategy.supportedBanks).willReturn(listOf(PartnerCode.LINE_BANK))
        given(strategy.filterAvailablePartners(any(), any())).willReturn(listOf(PartnerCode.LINE_BANK))
        given(generator.generate("LL")).willReturn("LL20260416ANWOW")
        given(strategy.toAdaptorRequest(any(), any())).willReturn(mock(LoanLimitAdaptorRequest::class.java))

        // when
        val response = loanLimitService.requestCompareLoan(request)

        // then
        assertThat(response.success).isEqualTo(true)
        then(loanLimitInquiryRepository).should().save(any<LoanLimitInquiry>())

        val eventCaptor = argumentCaptor<LoanLimitInquiryCreatedEvent>()
        then(applicationEventPublisher).should().publishEvent(eventCaptor.capture())
        assertThat(eventCaptor.firstValue.activePartnerCodes)
            .containsExactly(PartnerCode.LINE_BANK)
    }

    @Test
    @DisplayName("오토담보 - Nice DNR 조회 실패 시 진행 가능 금융사 없으면 예외")
    fun requestCompareLoan_auto_niceDnrFailed_throwException() {
        // given
        val request = create(
            1L,
            "윤교희",
            "9102131234556",
            "wEi9oYSuekQGxT9MV4rKHG4CO+Zrp+onhLIIuembI8jx/0PLF5Ne3oMBxvUFlN4UmsgjeNErZfmpCVUFH",
            JobType.EMPLOYEE,
            "오케이",
            LoanType.AUTO,
            true,
            LocalDateTime.now()
        )
        val strategy = mock(LoanLimitStrategy::class.java)
        given(strategyFactory.getStrategy(any())).willReturn(strategy)
        given(strategy.requiresExternalData()).willReturn(true)

        val externalDataContext = ofError(
            "NICE_DNR",
            create("NICE_DNR_ERROR", "NICE DNR 조회 오류")
        )
        given(strategy.fetchExternalData(any())).willReturn(externalDataContext)
        given(strategy.supportedBanks).willReturn(listOf(PartnerCode.LINE_BANK))
        given(strategy.filterAvailablePartners(any(), any())).willReturn(emptyList())
        given(generator.generate("LL")).willReturn("LL20260416ANWOW")

        // when & then
        assertThatThrownBy { loanLimitService.requestCompareLoan(request) }
            .isInstanceOf(InvalidRequestException::class.java)

        then(applicationEventPublisher).should(never()).publishEvent(any<LoanLimitInquiryCreatedEvent>())
    }
}
