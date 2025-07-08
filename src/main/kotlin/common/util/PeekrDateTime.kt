package com.peekr.common.util

import io.ktor.util.logging.KtorSimpleLogger
import java.time.Instant
import java.util.Date
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.CurrentTimestampBase
import org.jetbrains.exposed.sql.transactions.transaction

/**
 * Peekr 날짜/시간 유틸
 *
 * [Instant] 타입을 반환
 */
object PeekrDateTime {
    /**
     * DDL 용
     * ##### 사용 예시
     * ```
     * val createdAt = timestamp("created_at").defaultExpression(PeekrDateTime.timestamp)
     * ```
     */
    val timestamp: CurrentTimestampBase<Instant> = CurrentTimestamp

    private var cachedTime: Instant = Instant.now()
    private var lastUpdate: Long = 0L
    private const val CACHE_MS: Long = 1000L

    /**
     * 런타임에서 사용할 캐시된 DB 시간
     * ###### 사용 예시
     * ```
     * abstract class BaseEntityClass<E : BaseEntity>(table: BaseLongIdTable) : LongEntityClass<E>(table) {
     *     init {
     *         EntityHook.subscribe { action ->
     *             if (action.changeType == EntityChangeType.Updated) {
     *                 try {
     *                     // here...
     *                     action.toEntity(this)?.updatedAt = PeekrDateTime.now()
     *                 } catch (e: Exception) {
     *                     LOGGER.warn("Failed to update entity $this updatedAt:\n${e.message}")
     *                 }
     *             }
     *         }
     *     }
     * }
     * ```
     */
    fun now(): Instant {
        val current = System.currentTimeMillis()
        if (current - lastUpdate > CACHE_MS) {
            synchronized(this) {
                if (current - lastUpdate > CACHE_MS) {
                    try {
                        cachedTime = TimeQuery.getInstant()
                    } catch (e: Exception) {
                        // DB 에러 시 시스템 시간 사용
                        cachedTime = Instant.now()
                        LOGGER.warn("DB error, using system time: ${e.message}", e)
                    }
                    lastUpdate = current
                }
            }
        }
        return cachedTime
    }

    fun Instant.toDate(): Date = Date.from(this)
}

/**
 * DB 시간 관련 유틸
 * ##### 경고: 외부에서 사용하지 않는다.
 */
internal object TimeQuery {
    /**
     * DB의 시스템 타임스탬프를 [Instant]로 가져온다.
     * ##### 경고: 외부에서 사용하지 않는다.
     */
    fun getInstant(): Instant = transaction {
        exec("SELECT CURRENT_TIMESTAMP") { rs ->
            rs.next()
            rs.getTimestamp(1).toInstant()
        }!!
    }
}

private val LOGGER = KtorSimpleLogger("PeekrDateTime")
