package com.turnin.domain.feed.application.dto

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.util.Base64
import java.util.UUID
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedCursorCodecTest {
    @After
    fun tearDown() {
        unmockkStatic(UUID::class)
    }

    @Test
    fun `encode한 커서를 decodeOrNull로 복원하면 원본과 동일하다`() {
        val cursor = FeedCursor(
            seed = "abcdefg12345",
            sessionMaxId = 100L,
            windowAnchorId = 50L,
            lastShuffleKey = 42,
            lastUkId = 7L,
        )

        val encoded = FeedCursorCodec.encode(cursor)
        val decoded = FeedCursorCodec.decodeOrNull(encoded)

        assertEquals(cursor, decoded)
    }

    @Test
    fun `encode 결과는 URL-safe Base64 문자열이다`() {
        val cursor = FeedCursor(seed = "seed", sessionMaxId = 1L)

        val encoded = FeedCursorCodec.encode(cursor)

        // URL-safe Base64(패딩 없음)는 '+', '/', '=' 문자를 포함하지 않는다
        assertTrue(encoded.none { it == '+' || it == '/' || it == '=' })
    }

    @Test
    fun `null이 들어오면 null을 반환한다`() {
        assertNull(FeedCursorCodec.decodeOrNull(null))
    }

    @Test
    fun `빈 문자열이나 공백이 들어오면 null을 반환한다`() {
        assertNull(FeedCursorCodec.decodeOrNull(""))
        assertNull(FeedCursorCodec.decodeOrNull("   "))
    }

    @Test
    fun `Base64로 디코딩할 수 없는 문자열이면 null을 반환한다`() {
        assertNull(FeedCursorCodec.decodeOrNull("!!!not-base64!!!"))
    }

    @Test
    fun `Base64는 유효하지만 JSON 포맷이 아니면 null을 반환한다`() {
        val garbage = Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString("not a json".toByteArray(Charsets.UTF_8))

        assertNull(FeedCursorCodec.decodeOrNull(garbage))
    }

    @Test
    fun `알 수 없는 필드가 포함된 JSON도 ignoreUnknownKeys 설정 덕분에 정상 디코딩된다`() {
        val jsonWithExtraField = """
            {"seed":"seed-1","sessionMaxId":1,"windowAnchorId":null,"lastShuffleKey":null,"lastUkId":null,"unknownField":"ignored"}
        """.trimIndent()
        val encoded = Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(jsonWithExtraField.toByteArray(Charsets.UTF_8))

        val decoded = FeedCursorCodec.decodeOrNull(encoded)

        assertEquals("seed-1", decoded?.seed)
        assertEquals(1L, decoded?.sessionMaxId)
    }

    @Test
    fun `newSeed는 UUID 앞 12자리를 반환한다`() {
        val fixedUuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        mockkStatic(UUID::class)
        every { UUID.randomUUID() } returns fixedUuid

        val seed = FeedCursorCodec.newSeed()

        assertEquals(fixedUuid.toString().take(12), seed)
        assertEquals(12, seed.length)
    }

    @Test
    fun `newSeed는 호출할 때마다 다른 값을 생성한다`() {
        val seeds = (1..100).map { FeedCursorCodec.newSeed() }.toSet()

        // UUID 앞 12자리만 사용해 이론상 충돌 가능성은 있지만, 100회 반복에서는 사실상 무시 가능
        assertEquals(100, seeds.size)
    }
}
