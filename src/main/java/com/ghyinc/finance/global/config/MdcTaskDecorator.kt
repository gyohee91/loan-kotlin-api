package com.ghyinc.finance.global.config

import org.slf4j.MDC
import org.springframework.core.task.TaskDecorator
import java.util.*

/**
 * MDC 복사 후 작업 실행
 */
class MdcTaskDecorator : TaskDecorator {
    override fun decorate(runnable: Runnable): Runnable {
        // 호출 스레드의 MDC 스냅샷 복사
        val parentMdcContext = MDC.getCopyOfContextMap()
        return Runnable {
            try {
                // 작업 Thread에 MDC 복원
                if (Objects.nonNull(parentMdcContext)) {
                    MDC.setContextMap(parentMdcContext)
                }
                runnable.run()
            } finally {
                // 스레드풀 스레드는 재사용되므로 반드시 초기화
                MDC.clear()
            }
        }
    }
}
