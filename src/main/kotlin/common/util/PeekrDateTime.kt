package com.peekr.common.util

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.Date
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.CurrentTimestampBase

/**
 * Peekr 날짜/시간 유틸
 *
 * [Instant] 타입을 반환
 */
object PeekrDateTime {
    /**
     * DDL 용 DB의 CURRENT_TIMESTAMP 사용
     * ##### 사용 예시
     * ```
     * val createdAt = timestamp("created_at").defaultExpression(PeekrDateTime.timestamp)
     * ```
     */
    val timestamp: CurrentTimestampBase<Instant> = CurrentTimestamp

    /**
     * 런타임에서 사용할 현재 시간 (UTC)
     *
     * DB와 애플리케이션 서버가 모두 UTC로 설정되어 있다는 가정하에 시스템 시간을 그대로 사용한다.
     */
    fun now(): Instant = Instant.now()
}

fun Instant.toDate(): Date = Date.from(this)

fun Instant.toOffsetDateTime(): OffsetDateTime =
    this.atOffset(ZoneOffset.UTC)
