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
     * @param message 로그 메시지
     */
    fun AppLogger.debug(message: String) {
        val header = logger.name
        logger.debug("[$header]: $message")
    }

    /**
     * 오류 로그
     *
     * @param e [Throwable]
     * @param message 로그 메시지
     */
    fun AppLogger.error(e: Throwable, message: String? = null) {
        val header = logger.name
        val messageBody = message?.let { "message:\n$it" } ?: ""
        logger.error("[$header]:\nerror:\n${e.message}\n$messageBody")
    }
}
