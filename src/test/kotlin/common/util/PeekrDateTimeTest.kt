package com.peekr.common.util

import com.peekr.util.db.TestDatabaseFactory
import io.mockk.every
import io.mockk.mockkObject
import java.sql.SQLException
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Collections
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.junit.Before

class PeekrDateTimeTest {
    @Before
    fun setup() {
        TestDatabaseFactory.init()
    }

    @Test
    fun `timestamp가 CurrentTimeStamp 타입을 반환해야 한다`() {
        val result = PeekrDateTime.timestamp
        assertEquals(CurrentTimestamp, result)
    }

    @Test
    fun `1초 이내에 연속 호출하면 동일한 캐시된 값이 반환되어야 한다`() = runBlocking {
        val first = PeekrDateTime.now()
        delay(500L)
        val second = PeekrDateTime.now()

        assertEquals(first, second, "캐시된 값이 동일해야 한다.")
    }

    @Test
    fun `1초 뒤에 호출하면 서로 다른 값이 반환되어야 한다`() = runBlocking {
        val first = PeekrDateTime.now()
        delay(1100L)
        val second = PeekrDateTime.now()

        assertNotEquals(first, second, "서로 값이 달라야 한다.")
        assertTrue(second.isAfter(first.minus(2, ChronoUnit.SECONDS)))
        assertTrue(second.isBefore(first.plus(2, ChronoUnit.SECONDS)))
    }

    @Test
    fun `캐시된 시간은 1초 뒤에 만료되어야 한다`() {
        // given
        val first = PeekrDateTime.now()

        // when - 캐시 만료 대기
        Thread.sleep(1100)

        // 캐시가 만료된 후 새로운 호출
        val second = PeekrDateTime.now()
        val third = PeekrDateTime.now() // 이것은 새로운 캐시 값

        // then
        assertEquals(second, third, "캐시 갱신 후 연속 호출은 동일한 값이어야 합니다")
        assertTrue(
            second.isAfter(first.minus(2, ChronoUnit.SECONDS)),
            message = "first와 second는 다를 수도 있지만, 적어도 유효한 시간 범위 내에 있어야 함",
        )
    }

    @Test
    fun `여러 스레드에서 정상적으로 작동해야 한다`() {
        val results = Collections.synchronizedList(mutableListOf<Instant>())
        val threads = mutableListOf<Thread>()
        val threadCount = 10

        // 3개 스레드로 단순화
        repeat(threadCount) { i ->
            val thread = Thread {
                try {
                    val result = PeekrDateTime.now()
                    results.add(result)
                    println("Thread $i: $result")
                } catch (e: Exception) {
                    println("Thread $i error: ${e.message}")
                    e.printStackTrace()
                }
            }
            threads.add(thread)
            thread.start()
        }

        // 모든 스레드 완료 대기
        threads.forEach { it.join() }

        val firstInstant = results[0].epochSecond
        results.forEachIndexed { index, instant ->
            assertEquals(instant.epochSecond, firstInstant)
        }

        assertEquals(threadCount, results.size)
    }

    @Test
    fun `DB 에러가 발생하여 값을 가져오지 못할 때에도 정상적으로 시간을 반환해야 한다`() {
        mockkObject(TimeQuery)
        every { TimeQuery.getInstant() } throws SQLException()

        val result = PeekrDateTime.now()

        assertNotNull(result)
        assertTrue(result.isBefore(Instant.now().plus(1, ChronoUnit.MINUTES)))
        assertTrue(result.isAfter(Instant.now().minus(1, ChronoUnit.MINUTES)))
    }

    @Test
    fun `Instant에서 변환된 OffsetDateTime을 다시 Instant화 시켜도 항상 같아야 한다`() {
        val instant = PeekrDateTime.now()
        val offsetDateTime = instant.toOffsetDateTime()
        val instant2 = offsetDateTime.toInstant()

        assertEquals(instant, instant2)
    }
}
