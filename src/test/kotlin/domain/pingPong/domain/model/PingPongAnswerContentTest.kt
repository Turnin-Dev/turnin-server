package com.turnin.domain.pingPong.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.jupiter.api.assertThrows

class PingPongAnswerContentTest {
    @Test
    fun `답변 내용이 2200자이면 정상적으로 생성된다`() {
        // given
        val content = "a".repeat(2200)

        // when
        val result = PingPongAnswerContent(content)

        // then
        assertEquals(2200, result.value.length)
    }

    @Test
    fun `답변 내용이 2201자이면 유효성 검사 예외가 발생한다`() {
        // given
        val tooLongContent = "a".repeat(2201)

        // when, then
        assertThrows<PingPongAnswerContentValidationException> {
            PingPongAnswerContent(tooLongContent)
        }
    }

    @Test
    fun `답변 내용이 1자이면 정상적으로 생성된다`() {
        // when
        val result = PingPongAnswerContent("a")

        // then
        assertEquals("a", result.value)
    }

    @Test
    fun `답변 내용이 빈 문자열이면 유효성 검사 예외가 발생한다`() {
        // when, then
        assertThrows<PingPongAnswerContentValidationException> {
            PingPongAnswerContent("")
        }
    }

    @Test
    fun `답변 내용이 공백으로만 이루어져 있으면 유효성 검사 예외가 발생한다`() {
        // when, then
        assertThrows<PingPongAnswerContentValidationException> {
            PingPongAnswerContent(" \n\t")
        }
    }
}
