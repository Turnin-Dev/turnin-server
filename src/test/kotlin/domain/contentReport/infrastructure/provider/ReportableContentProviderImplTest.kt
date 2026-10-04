package com.turnin.domain.contentReport.infrastructure.provider

import com.turnin.common.model.ContentReportType
import com.turnin.common.model.id.PingPongAnswerId
import com.turnin.common.model.id.PingPongId
import com.turnin.common.model.id.PingPongIdValidationException
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.contentReport.domain.model.ReportableContent
import com.turnin.domain.pingPong.application.provider.PingPongContentReportApi
import com.turnin.domain.pingPong.domain.model.PingPong
import com.turnin.domain.pingPong.domain.model.PingPongAnswer
import com.turnin.domain.pingPong.domain.model.PingPongAnswerWithAnswerer
import com.turnin.domain.pingPong.domain.model.PingPongContent
import com.turnin.domain.pingPong.domain.repository.PingPongRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertThrows

class ReportableContentProviderImplTest {
    private val pingPongRepository = mockk<PingPongRepository>()
    private val provider = ReportableContentProviderImpl(PingPongContentReportApi(pingPongRepository))

    @Test
    fun `핑퐁 질문 조회 시 질문자를 작성자로, 질문 내용을 스냅샷으로 반환한다`() = runTest {
        // given
        coEvery { pingPongRepository.findVisibleById(PingPongId(10L)) } returns PingPong(
            id = PingPongId(10L),
            userKeywordId = UserKeywordId(3L),
            questionerId = UserId(1L),
            question = PingPongContent("질문 내용"),
            createdAt = 1000L,
            updatedAt = 1000L,
        )

        // when
        val result = provider.findVisible(ContentReportType.PING_PONG_QUESTION, 10L)

        // then
        assertEquals(ReportableContent(authorId = UserId(1L), snapshot = "질문 내용"), result)
    }

    @Test
    fun `핑퐁 답변 조회 시 답변자를 작성자로, 답변 내용을 스냅샷으로 반환한다`() = runTest {
        // given
        coEvery { pingPongRepository.findVisibleAnswerWithAnswererById(PingPongAnswerId(20L)) } returns
            PingPongAnswerWithAnswerer(
                answer = PingPongAnswer(
                    id = PingPongAnswerId(20L),
                    pingPongId = PingPongId(10L),
                    answer = PingPongContent("답변 내용"),
                    createdAt = 2000L,
                    updatedAt = 2000L,
                ),
                answererId = UserId(2L),
            )

        // when
        val result = provider.findVisible(ContentReportType.PING_PONG_ANSWER, 20L)

        // then
        assertEquals(ReportableContent(authorId = UserId(2L), snapshot = "답변 내용"), result)
    }

    @Test
    fun `핑퐁 질문 조회 시 질문이 없으면 null을 반환한다`() = runTest {
        // given
        coEvery { pingPongRepository.findVisibleById(PingPongId(10L)) } returns null

        // when
        val result = provider.findVisible(ContentReportType.PING_PONG_QUESTION, 10L)

        // then
        assertNull(result)
    }

    @Test
    fun `핑퐁 답변 조회 시 답변이 없으면 null을 반환한다`() = runTest {
        // given
        coEvery { pingPongRepository.findVisibleAnswerWithAnswererById(PingPongAnswerId(20L)) } returns null

        // when
        val result = provider.findVisible(ContentReportType.PING_PONG_ANSWER, 20L)

        // then
        assertNull(result)
    }

    @Test
    fun `핑퐁 질문 숨김 처리 시 해당 질문을 숨김 처리한다`() = runTest {
        // given
        coEvery { pingPongRepository.hideQuestion(PingPongId(10L)) } returns true

        // when
        provider.hide(ContentReportType.PING_PONG_QUESTION, 10L)

        // then
        coVerify(exactly = 1) { pingPongRepository.hideQuestion(PingPongId(10L)) }
    }

    @Test
    fun `핑퐁 답변 숨김 처리 시 해당 답변을 숨김 처리한다`() = runTest {
        // given
        coEvery { pingPongRepository.hideAnswer(PingPongAnswerId(20L)) } returns true

        // when
        provider.hide(ContentReportType.PING_PONG_ANSWER, 20L)

        // then
        coVerify(exactly = 1) { pingPongRepository.hideAnswer(PingPongAnswerId(20L)) }
    }

    @Test
    fun `콘텐츠 ID가 0 이하이면 유효성 검사 예외가 발생한다`() = runTest {
        // given
        val invalidContentId = 0L

        // when, then
        assertThrows<PingPongIdValidationException> {
            provider.findVisible(ContentReportType.PING_PONG_QUESTION, invalidContentId)
        }
    }
}
