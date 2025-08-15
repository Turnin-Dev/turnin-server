package com.peekr.common.util

import io.ktor.util.logging.KtorSimpleLogger
import org.slf4j.Logger

/**
 * 애플리케이션 로거 타입
 */
data class AppLogger(val logger: Logger)

/**
 * 애플리케이션 로거 팩토리
 */
object AppLoggerFactory {
    /**
     * 로거를 생성한다.
     *
     * 로거 형태는 언제든지 바뀔 수 있다.
     */
    fun createLogger(name: String): AppLogger = AppLogger(KtorSimpleLogger(name))

    /**
     * 디버깅 로그
     *
     * @param header 로그 앞 부분 설명
     * @param message 로그 메시지
     */
    fun AppLogger.debug(header: String, message: String) = logger.debug(message)
}
