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

    /**
     * MDC 태그를 임시로 주입하고 블록 실행 후 이전 상태로 복원하는 함수
     *
     * 중첩 호출 시 외부 컨텍스트의 MDC 값을 손상시키지 않도록 기존 값을 저장 후 복원한다.
     *
     * ⚠️ 추후 requestId 등 요청 추적용 값을 MDC에 추가할 경우:
     * - 코루틴에서 Dispatchers.IO 등으로 스레드 전환 시 MDC 값이 유실될 수 있음
     * - `kotlinx-coroutines-slf4j` 의존성 추가 후 `MDCContext()`를 코루틴 컨텍스트에 주입 필요
     * - 참고: https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-slf4j/
     */
    private fun withTags(tags: Map<String, String>, block: () -> Unit) {
        val previousValues = tags.keys.associateWith { MDC.get(it) }
        tags.forEach { (k, v) -> MDC.put(k, v) }
        try {
            block()
        } finally {
            previousValues.forEach { (k, v) ->
                if (v == null) MDC.remove(k) else MDC.put(k, v)
            }
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
