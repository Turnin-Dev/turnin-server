package com.peekr.common.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 애플리케이션 로거 타입
 */
class AppLogger(private val logger: Logger) {
    /**
     * 디버깅 로그
     *
     * @param message 로그 메시지
     * @param e [Throwable]
     */
    fun debug(message: String, e: Throwable? = null) {
        if (e != null) logger.debug(message, e) else logger.debug(message)
    }

    /**
     * 오류 로그
     *
     * @param e [Throwable]
     * @param message 로그 메시지
     */
    fun error(e: Throwable, message: String?) {
        logger.error(message, e)
    }

    /**
     * 오류 로그 (호환용 오버로드 메서드)
     *
     * @param message 로그 메시지
     */
    fun error(message: String) {
        logger.error(message)
    }

    /**
     * 경고 로그
     *
     * @param message 로그 메시지
     * @param e [Throwable]
     */
    fun warn(message: String, e: Throwable? = null) {
        if (e != null) logger.warn(message, e) else logger.warn(message)
    }

    /**
     * 정보 로그
     *
     * @param message 로그 메시지
     * @param e [Throwable]
     */
    fun info(message: String, e: Throwable? = null) {
        if (e != null) logger.info(message, e) else logger.info(message)
    }
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
    fun createLogger(name: String): AppLogger = AppLogger(LoggerFactory.getLogger(name))

    inline fun <reified T> createLogger(): AppLogger = createLogger(T::class.java.name)
}
