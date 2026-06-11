package com.turnin.common.batch

import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.jupiter.api.Assertions.assertTrue

class DelayUntilNextRunTest {
    private val kst = ZoneId.of("Asia/Seoul")

    // 고정 시각으로 Clock을 만드는 헬퍼
    // e.g. fixedClock("2024-01-15T00:30:00") → KST 00:30
    private fun fixedClock(kstDateTimeIso: String): Clock {
        val instant = LocalDateTime
            .parse(kstDateTimeIso)
            .atZone(kst)
            .toInstant()
        return Clock.fixed(instant, kst)
    }

    // ================ 대기 시간 계산 검증 ================

    @Test
    fun `현재 시각이 목표 시각 이전이면 당일 목표 시각까지 대기한다`() = runTest {
        // KST 00:30 → 목표 01:00 → 30분 대기
        val clock = fixedClock("2024-01-15T00:30:00")
        var capturedDelayMs = 0L

        delayUntilNextRun(
            kstHour = 1,
            kstMinute = 0,
            clock = clock,
            delayFn = { capturedDelayMs = it },
        )

        val expectedMs = 30 * 60 * 1000L
        assertEquals(expectedMs, capturedDelayMs)
    }

    @Test
    fun `현재 시각이 목표 시각 이후이면 다음 날 목표 시각까지 대기한다`() = runTest {
        // KST 01:30 → 목표 01:00은 이미 지남 → 다음날 01:00까지 23시간 30분 대기
        val clock = fixedClock("2024-01-15T01:30:00")
        var capturedDelayMs = 0L

        delayUntilNextRun(
            kstHour = 1,
            kstMinute = 0,
            clock = clock,
            delayFn = { capturedDelayMs = it },
        )

        val expectedMs = (23 * 60 + 30) * 60 * 1000L // 23시간 30분
        assertEquals(expectedMs, capturedDelayMs)
    }

    @Test
    fun `현재 시각이 목표 시각과 정확히 같으면 즉시 실행된다 (대기 없음)`() = runTest {
        // KST 01:00 정각
        val clock = fixedClock("2024-01-15T01:00:00")
        var capturedDelayMs = -1L

        delayUntilNextRun(
            kstHour = 1,
            kstMinute = 0,
            clock = clock,
            delayFn = { capturedDelayMs = it },
        )

        assertEquals(0L, capturedDelayMs)
    }

    @Test
    fun `자정 직전 23시 59분에 목표가 00시 00분이면 1분 대기한다`() = runTest {
        val clock = fixedClock("2024-01-15T23:59:00")
        var capturedDelayMs = 0L

        delayUntilNextRun(
            kstHour = 0,
            kstMinute = 0,
            clock = clock,
            delayFn = { capturedDelayMs = it },
        )

        val expectedMs = 60 * 1000L
        assertEquals(expectedMs, capturedDelayMs)
    }

    @Test
    fun `자정 직후 00시 01분에 목표가 00시 00분이면 23시간 59분 대기한다`() = runTest {
        val clock = fixedClock("2024-01-15T00:01:00")
        var capturedDelayMs = 0L

        delayUntilNextRun(
            kstHour = 0,
            kstMinute = 0,
            clock = clock,
            delayFn = { capturedDelayMs = it },
        )

        val expectedMs = (23 * 60 + 59) * 60 * 1000L
        assertEquals(expectedMs, capturedDelayMs)
    }

    // ================ delayFn 호출 횟수 검증 ================

    @Test
    fun `delayFn은 정확히 한 번만 호출된다`() = runTest {
        val clock = fixedClock("2024-01-15T00:30:00")
        var callCount = 0

        delayUntilNextRun(
            kstHour = 1,
            kstMinute = 0,
            clock = clock,
            delayFn = { callCount++ },
        )

        assertEquals(1, callCount)
    }

    // ================ 대기 시간 범위 검증 (경계값) ================

    @Test
    fun `대기 시간은 항상 0 이상 24시간 미만이다`() = runTest {
        val clock = fixedClock("2024-01-15T12:00:00")
        var capturedDelayMs = 0L

        delayUntilNextRun(
            kstHour = 1,
            kstMinute = 0,
            clock = clock,
            delayFn = { capturedDelayMs = it },
        )

        val oneDayMs = 24 * 60 * 60 * 1000L
        assertTrue(capturedDelayMs >= 0L) { "delay must be non-negative" }
        assertTrue(capturedDelayMs < oneDayMs) { "delay must be less than 24 hours" }
    }
}
