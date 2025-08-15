package com.peekr.common.util

import io.ktor.util.logging.KtorSimpleLogger
import org.slf4j.Logger

/**
 * 애플리케이션 로거 타입
 */
class AppLogger(val logger: Logger)

/**
 * 디버깅 로그
 *
 * @param message 로그 메시지
 */
fun AppLogger.debug(message: String, e: Throwable? = null) {
    logger.debug(message, e)
}

/**
 * 오류 로그
 *
 * @param e [Throwable]
 * @param message 로그 메시지
 */
fun AppLogger.error(e: Throwable, message: String? = null) {
    logger.error(message, e)
}

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
}
