package com.turnin.common.util

import com.turnin.util.db.TestDatabaseFactory
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Collections
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.junit.Before

class TurninDateTimeTest {
    @Before
    fun setup() {
        TestDatabaseFactory.init()
    }

    @Test
    fun `timestamp가 CurrentTimeStamp 타입을 반환해야 한다`() {
        val result = TurninDateTime.timestamp
        assertEquals(CurrentTimestamp, result)
    }

    @Test
    fun `1초 뒤에 호출하면 서로 다른 값이 반환되어야 한다`() = runBlocking {
        val first = TurninDateTime.now()
        delay(1100L)
        val second = TurninDateTime.now()

        assertNotEquals(first, second, "서로 값이 달라야 한다.")
        assertTrue(second.isAfter(first.minus(2, ChronoUnit.SECONDS)))
        assertTrue(second.isBefore(first.plus(2, ChronoUnit.SECONDS)))
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
                    val result = TurninDateTime.now()
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
    fun `Instant에서 변환된 OffsetDateTime을 다시 Instant화 시켜도 항상 같아야 한다`() {
        val instant = TurninDateTime.now()
        val offsetDateTime = instant.toOffsetDateTime()
        val instant2 = offsetDateTime.toInstant()

        assertEquals(instant, instant2)
    }
}
