package com.turnin.common.util.pagination.cursor

import io.mockk.every
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import java.util.Base64
import java.util.UUID
import kotlinx.serialization.Serializable
import org.junit.After
import org.junit.Assert
import org.junit.Test

@Serializable
data class TestCursorA(
    val label: String,
    val value: Long,
    val nested: Int? = null,
)

@Serializable
data class TestCursorB(
    val name: String,
    val score: Double,
)

class CursorCodecTest {
    @After
    fun tearDown() {
        unmockkStatic(UUID::class)
    }

    @Test
    fun `encode한 커서를 decodeOrNull로 복원하면 원본과 동일하다`() {
        val cursor = TestCursorA(label = "abcdefg12345", value = 100L, nested = 42)

        val encoded = CursorCodec.encode(cursor)
        val decoded = CursorCodec.decodeOrNull<TestCursorA>(encoded)

        Assert.assertEquals(cursor, decoded)
    }

    @Test
    fun `다른 타입의 커서도 동일한 유틸리티로 encode decode가 가능하다`() {
        val cursor = TestCursorB(name = "seed-1", score = 0.87)

        val encoded = CursorCodec.encode(cursor)
        val decoded = CursorCodec.decodeOrNull<TestCursorB>(encoded)

        Assert.assertEquals(cursor, decoded)
    }

    @Test
    fun `서로 다른 타입끼리는 디코딩이 섞이지 않는다`() {
        val cursor = TestCursorA(label = "seed", value = 1L)
        val encoded = CursorCodec.encode(cursor)

        val decoded = CursorCodec.decodeOrNull<TestCursorB>(encoded)

        Assert.assertNull(decoded)
    }

    @Test
    fun `encode 결과는 URL-safe Base64 문자열이다`() {
        val cursor = TestCursorA(label = "seed", value = 1L)

        val encoded = CursorCodec.encode(cursor)

        // URL-safe Base64(패딩 없음)는 '+', '/', '=' 문자를 포함하지 않음
        Assert.assertTrue(encoded.none { it == '+' || it == '/' || it == '=' })
    }

    @Test
    fun `null이 들어오면 null을 반환한다`() {
        Assert.assertNull(CursorCodec.decodeOrNull<TestCursorA>(null))
    }

    @Test
    fun `빈 문자열이나 공백이 들어오면 null을 반환한다`() {
        Assert.assertNull(CursorCodec.decodeOrNull<TestCursorA>(""))
        Assert.assertNull(CursorCodec.decodeOrNull<TestCursorA>("   "))
    }

    @Test
    fun `Base64로 디코딩할 수 없는 문자열이면 null을 반환한다`() {
        Assert.assertNull(CursorCodec.decodeOrNull<TestCursorA>("!!!not-base64!!!"))
    }

    @Test
    fun `Base64는 유효하지만 JSON 포맷이 아니면 null을 반환한다`() {
        val garbage = Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString("not a json".toByteArray(Charsets.UTF_8))

        Assert.assertNull(CursorCodec.decodeOrNull<TestCursorA>(garbage))
    }

    @Test
    fun `알 수 없는 필드가 포함된 JSON도 ignoreUnknownKeys 설정 덕분에 정상 디코딩된다`() {
        val jsonWithExtraField = """
            {"label":"seed-1","value":1,"nested":null,"unknownField":"ignored"}
        """.trimIndent()
        val encoded = Base64
            .getUrlEncoder()
            .withoutPadding()
            .encodeToString(jsonWithExtraField.toByteArray(Charsets.UTF_8))

        val decoded = CursorCodec.decodeOrNull<TestCursorA>(encoded)

        Assert.assertEquals("seed-1", decoded?.label)
        Assert.assertEquals(1L, decoded?.value)
    }

    @Test
    fun `newSeed는 UUID 앞 12자리를 반환한다`() {
        val fixedUuid = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        mockkStatic(UUID::class)
        every { UUID.randomUUID() } returns fixedUuid

        val seed = CursorCodec.newSeed()

        Assert.assertEquals(fixedUuid.toString().take(12), seed)
        Assert.assertEquals(12, seed.length)
    }

    @Test
    fun `newSeed는 호출할 때마다 다른 값을 생성한다`() {
        val seeds = (1..100).map { CursorCodec.newSeed() }.toSet()

        // UUID 앞 12자리만 사용해 이론상 충돌 가능성은 있지만, 100회 반복에서는 사실상 무시 가능
        Assert.assertEquals(100, seeds.size)
    }
}
