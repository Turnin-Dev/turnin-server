package com.turnin.domain.pingPong.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import org.junit.jupiter.api.assertThrows

class PingPongQuestionContentTest {
    @Test
    fun `질문 내용이 2200자이면 정상적으로 생성된다`() {
        // given
        val content = "a".repeat(2200)

        // when
        val result = PingPongQuestionContent(content)

        // then
        assertEquals(2200, result.value.length)
    }

    @Test
    fun `질문 내용이 2201자이면 유효성 검사 예외가 발생한다`() {
        // given
        val tooLongContent = "a".repeat(2201)

        // when, then
        assertThrows<PingPongQuestionContentValidationException> {
            PingPongQuestionContent(tooLongContent)
        }
    }

    @Test
    fun `질문 내용이 1자이면 정상적으로 생성된다`() {
        // when
        val result = PingPongQuestionContent("a")

        // then
        assertEquals("a", result.value)
    }

    @Test
    fun `질문 내용이 빈 문자열이면 유효성 검사 예외가 발생한다`() {
        // when, then
        assertThrows<PingPongQuestionContentValidationException> {
            PingPongQuestionContent("")
        }
    }

    @Test
    fun `질문 내용이 공백으로만 이루어져 있으면 유효성 검사 예외가 발생한다`() {
        // when, then
        assertThrows<PingPongQuestionContentValidationException> {
            PingPongQuestionContent(" \n\t")
        }
    }

    @Test
    fun `질문 생성 시 끝에 물음표가 없으면 물음표를 붙인다`() {
        // when
        val result = PingPongQuestionContent.create("질문")

        // then
        assertEquals("질문?", result.value)
    }

    @Test
    fun `질문 생성 시 이미 물음표로 끝나면 그대로 둔다`() {
        // when
        val result = PingPongQuestionContent.create("질문?")

        // then
        assertEquals("질문?", result.value)
    }

    @Test
    fun `질문 생성 시 앞뒤 공백을 제거한 뒤 물음표 여부를 판단한다`() {
        // when
        val result = PingPongQuestionContent.create(" 질문? ")

        // then
        assertEquals("질문?", result.value)
    }

    @Test
    fun `질문 생성 시 물음표로 끝나지 않는 문장부호 뒤에는 물음표를 붙인다`() {
        // when
        val result = PingPongQuestionContent.create("질문?!")

        // then
        assertEquals("질문?!?", result.value)
    }

    @Test
    fun `질문 생성 시 물음표 두 개만 있으면 그대로 생성된다`() {
        // when
        val result = PingPongQuestionContent.create("??")

        // then
        assertEquals("??", result.value)
    }

    @Test
    fun `질문 생성 시 물음표 하나만 있으면 유효성 검사 예외가 발생한다`() {
        // when, then
        assertThrows<PingPongQuestionContentValidationException> {
            PingPongQuestionContent.create("?")
        }
    }

    @Test
    fun `질문 생성 시 공백과 물음표 하나만 있으면 유효성 검사 예외가 발생한다`() {
        // when, then
        assertThrows<PingPongQuestionContentValidationException> {
            PingPongQuestionContent.create(" ? ")
        }
    }

    @Test
    fun `질문 생성 시 공백으로만 이루어져 있으면 유효성 검사 예외가 발생한다`() {
        // when, then
        assertThrows<PingPongQuestionContentValidationException> {
            PingPongQuestionContent.create(" \n\t")
        }
    }

    @Test
    fun `질문 생성 시 물음표를 붙여 2200자이면 정상적으로 생성된다`() {
        // given
        val question = "a".repeat(2199)

        // when
        val result = PingPongQuestionContent.create(question)

        // then
        assertEquals(2200, result.value.length)
        assertEquals('?', result.value.last())
    }

    @Test
    fun `질문 생성 시 물음표를 붙여 2201자가 되면 유효성 검사 예외가 발생한다`() {
        // given
        val question = "a".repeat(2200)

        // when, then
        assertThrows<PingPongQuestionContentValidationException> {
            PingPongQuestionContent.create(question)
        }
    }
}
