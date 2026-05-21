package com.ghyinc.finance.domain.loan.factory

import com.ghyinc.finance.domain.loan.enums.LoanType
import com.ghyinc.finance.domain.loan.strategy.LoanLimitStrategy
import org.apache.kafka.common.errors.InvalidRequestException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

/**
 * 대출 유형에 따른 Strategy 반환 팩토리
 *
 *
 * Spring의 DI를 활용하여 LoanLimitStrategy 구현체를 자동 수집
 * 새로운 대출 유형이 추가되더라도 Factory 코드 수정 없이
 * Strategy 구현체만 추가하면 자동으로 등록됨 (OCP 준수)
 *
 * <pre>
 * [등록 방식]
 * 생성자에서 List<LoanLimitStrategy>를 주입받아
 * Strategy 구현체만 추가하면 자동으로 등록됨 (OCP 준수)
</LoanLimitStrategy></pre> *
 */
@Component
class LoanLimitStrategyFactory(strategies: List<LoanLimitStrategy>) {
    private val log = LoggerFactory.getLogger(LoanLimitStrategyFactory::class.java)
    private val strategyMap: Map<LoanType, LoanLimitStrategy> =
        strategies.associateBy { it.loanType }

    init {
        log.info("LoanLimitStrategyFactory 초기화 완료. 등록된 전략: {}", strategyMap.keys)
    }

    /**
     * 대출 유형에 해당하는 전략 반환
     * @param loanType
     * @throws InvalidRequestException 지원하지 않는 대출 유형인 경우
     * @return
     */
    fun getStrategy(loanType: LoanType): LoanLimitStrategy {
        return strategyMap[loanType]
            ?: run {
                log.error("지원하지 않는 대출 유형. loanType: {}", loanType)
                throw InvalidRequestException("지원하지 않는 대출 유형입니다: $loanType")
            }
    }
}
