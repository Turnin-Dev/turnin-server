package com.turnin.domain.pingPong.application.usecase

import com.turnin.common.model.id.PingPongId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.common.model.id.UserKeywordIdValidationException
import com.turnin.domain.pingPong.application.dto.PingPongDto
import com.turnin.domain.pingPong.domain.model.PingPong
import com.turnin.domain.pingPong.domain.model.PingPongContent
import com.turnin.domain.pingPong.domain.model.PingPongContentValidationException
import com.turnin.domain.pingPong.domain.provider.UserKeywordProvider
import com.turnin.domain.pingPong.domain.repository.PingPongRepository
import com.turnin.domain.pingPong.exception.PingPongException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertThrows

class CreatePingPongUseCaseTest {
    private val pingPongRepository = mockk<PingPongRepository>()
    private val userKeywordProvider = mockk<UserKeywordProvider>()
    private val usecase = CreatePingPongUseCase(pingPongRepository, userKeywordProvider)

    @Test
    fun `타인의 게시물에 질문을 작성하면 생성된 핑퐁을 반환한다`() = runTest {
        // given
        coEvery { userKeywordProvider.findOwnerId(UserId(1L), UserKeywordId(3L)) } returns TestOwnerId
        coEvery {
            pingPongRepository.create(UserKeywordId(3L), UserId(1L), PingPongContent("질문 내용"))
        } returns PingPong(
            id = PingPongId(10L),
            userKeywordId = UserKeywordId(3L),
            questionerId = UserId(1L),
            question = PingPongContent("질문 내용"),
            createdAt = 1000L,
            updatedAt = 1000L,
        )

        // when
        val result = usecase(UserId(1L), 3L, "질문 내용")

        // then
        val expected = PingPongDto(
            id = 10L,
            userKeywordId = 3L,
            questionerId = 1L,
            question = "질문 내용",
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        assertEquals(expected, result)
    }

    @Test
    fun `질문 작성 시 요청한 게시물 ID와 질문자 ID, 질문 내용으로 핑퐁을 저장한다`() = runTest {
        // given
        coEvery { userKeywordProvider.findOwnerId(UserId(1L), UserKeywordId(3L)) } returns TestOwnerId
        coEvery {
            pingPongRepository.create(UserKeywordId(3L), UserId(1L), PingPongContent("질문 내용"))
        } returns TestPingPong

        // when
        usecase(UserId(1L), 3L, "질문 내용")

        // then
        coVerify(exactly = 1) {
            pingPongRepository.create(UserKeywordId(3L), UserId(1L), PingPongContent("질문 내용"))
        }
    }

    @Test
    fun `게시물을 조회할 수 없으면 게시물 없음 예외가 발생하고 핑퐁을 저장하지 않는다`() = runTest {
        // given
        coEvery { userKeywordProvider.findOwnerId(UserId(1L), UserKeywordId(3L)) } returns null

        // when, then
        assertThrows<PingPongException.UserKeywordNotFound> {
            usecase(UserId(1L), 3L, "질문 내용")
        }
        coVerify(exactly = 0) {
            pingPongRepository.create(UserKeywordId(3L), UserId(1L), PingPongContent("질문 내용"))
        }
    }

    @Test
    fun `본인 게시물에 질문을 작성하면 예외가 발생하고 핑퐁을 저장하지 않는다`() = runTest {
        // given
        coEvery { userKeywordProvider.findOwnerId(UserId(1L), UserKeywordId(3L)) } returns UserId(1L)

        // when, then
        assertThrows<PingPongException.CannotQuestionOwnUserKeyword> {
            usecase(UserId(1L), 3L, "질문 내용")
        }
        coVerify(exactly = 0) {
            pingPongRepository.create(UserKeywordId(3L), UserId(1L), PingPongContent("질문 내용"))
        }
    }

    @Test
    fun `질문 내용이 유효하지 않으면 유효성 검사 예외가 발생한다`() = runTest {
        // given
        val blankQuestion = " "

        // when, then
        assertThrows<PingPongContentValidationException> {
            usecase(UserId(1L), 3L, blankQuestion)
        }
    }

    @Test
    fun `게시물 ID가 0 이하이면 유효성 검사 예외가 발생한다`() = runTest {
        // given
        val invalidUserKeywordId = 0L

        // when, then
        assertThrows<UserKeywordIdValidationException> {
            usecase(UserId(1L), invalidUserKeywordId, "질문 내용")
        }
    }

    companion object {
        private val TestOwnerId = UserId(2L)
        private val TestPingPong = PingPong(
            id = PingPongId(10L),
            userKeywordId = UserKeywordId(3L),
            questionerId = UserId(1L),
            question = PingPongContent("질문 내용"),
            createdAt = 1000L,
            updatedAt = 1000L,
        )
    }
}
