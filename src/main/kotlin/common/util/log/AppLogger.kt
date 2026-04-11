package com.peekr.common.util.log

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC

/**
 * 애플리케이션 로거
 */
class AppLogger(private val logger: Logger) {
    // ------------------------------ 태그와 함께 로그 (기본) ------------------------------
    fun debug(message: String, tags: Map<String, String> = emptyMap(), e: Throwable? = null) {
        withTags(tags) { if (e != null) logger.debug(message, e) else logger.debug(message) }
    }

    fun info(message: String, tags: Map<String, String> = emptyMap(), e: Throwable? = null) {
        withTags(tags) { if (e != null) logger.info(message, e) else logger.info(message) }
    }

    fun warn(message: String, tags: Map<String, String> = emptyMap(), e: Throwable? = null) {
        withTags(tags) { if (e != null) logger.warn(message, e) else logger.warn(message) }
    }

    fun error(message: String, tags: Map<String, String> = emptyMap(), e: Throwable? = null) {
        withTags(tags) { if (e != null) logger.error(message, e) else logger.error(message) }
    }

    // ------------------------------ 기존 호환용 (태그 없음) ------------------------------
    // 내부적으로 위 메서드들을 호출하여 중복을 제거합니다.
    fun debug(message: String, e: Throwable? = null) = debug(message, emptyMap(), e)

    fun info(message: String, e: Throwable? = null) = info(message, emptyMap(), e)

    fun warn(message: String, e: Throwable? = null) = warn(message, emptyMap(), e)

    fun error(message: String) = error(message, emptyMap(), null)

    fun error(e: Throwable, message: String?) = error(message ?: "Error occurred", emptyMap(), e)

    // 공통 태그 주입 로직
    private fun withTags(tags: Map<String, String>, block: () -> Unit) {
        tags.forEach { (k, v) -> MDC.put(k, v) }
        try {
            block()
        } finally {
            tags.keys.forEach { MDC.remove(it) }
        }
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
