package com.turnin.domain.pingPong.application.usecase

import com.turnin.common.db.DatabaseException
import com.turnin.common.model.NotificationType
import com.turnin.common.model.UserName
import com.turnin.common.model.id.PingPongAnswerId
import com.turnin.common.model.id.PingPongId
import com.turnin.common.model.id.PingPongIdValidationException
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.pingPong.application.dto.PingPongAnswerDto
import com.turnin.domain.pingPong.domain.model.PingPong
import com.turnin.domain.pingPong.domain.model.PingPongAnswer
import com.turnin.domain.pingPong.domain.model.PingPongContent
import com.turnin.domain.pingPong.domain.model.PingPongContentValidationException
import com.turnin.domain.pingPong.domain.model.PingPongNotificationCommand
import com.turnin.domain.pingPong.domain.provider.BlockProvider
import com.turnin.domain.pingPong.domain.provider.NotificationProvider
import com.turnin.domain.pingPong.domain.provider.UserKeywordProvider
import com.turnin.domain.pingPong.domain.provider.UserProvider
import com.turnin.domain.pingPong.domain.repository.PingPongRepository
import com.turnin.domain.pingPong.exception.PingPongException
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import java.sql.SQLException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertThrows

@OptIn(ExperimentalCoroutinesApi::class)
class CreatePingPongAnswerUseCaseTest {
    private val pingPongRepository = mockk<PingPongRepository>()
    private val userKeywordProvider = mockk<UserKeywordProvider>()
    private val blockProvider = mockk<BlockProvider>()
    private val userProvider = mockk<UserProvider>()
    private val notificationProvider = mockk<NotificationProvider>()

    // 알림 전송 코루틴은 advanceUntilIdle() 호출 시에만 실행된다.
    private val applicationScope = TestScope()
    private val usecase = CreatePingPongAnswerUseCase(
        pingPongRepository,
        userKeywordProvider,
        blockProvider,
        userProvider,
        notificationProvider,
        applicationScope,
    )

    @Test
    fun `게시물 작성자가 답변을 작성하면 생성된 답변을 반환한다`() = runTest {
        // given
        coEvery { pingPongRepository.findVisibleById(PingPongId(10L)) } returns TestPingPong
        coEvery { userKeywordProvider.findOwnerId(UserId(2L), UserKeywordId(3L)) } returns UserId(2L)
        coEvery { blockProvider.isBlockedRelationship(UserId(2L), UserId(1L)) } returns false
        coEvery {
            pingPongRepository.createAnswer(PingPongId(10L), PingPongContent("답변 내용"))
        } returns PingPongAnswer(
            id = PingPongAnswerId(20L),
            pingPongId = PingPongId(10L),
            answer = PingPongContent("답변 내용"),
            createdAt = 2000L,
            updatedAt = 2000L,
        )

        // when
        val result = usecase(UserId(2L), 10L, "답변 내용")

        // then
        val expected = PingPongAnswerDto(
            id = 20L,
            pingPongId = 10L,
            answer = "답변 내용",
            createdAt = 2000L,
            updatedAt = 2000L,
        )
        assertEquals(expected, result)
    }

    @Test
    fun `답변 작성 시 요청한 핑퐁 ID와 답변 내용으로 답변을 저장한다`() = runTest {
        // given
        coEvery { pingPongRepository.findVisibleById(PingPongId(10L)) } returns TestPingPong
        coEvery { userKeywordProvider.findOwnerId(UserId(2L), UserKeywordId(3L)) } returns UserId(2L)
        coEvery { blockProvider.isBlockedRelationship(UserId(2L), UserId(1L)) } returns false
        coEvery {
            pingPongRepository.createAnswer(PingPongId(10L), PingPongContent("답변 내용"))
        } returns TestAnswer

        // when
        usecase(UserId(2L), 10L, "답변 내용")

        // then
        coVerify(exactly = 1) {
            pingPongRepository.createAnswer(PingPongId(10L), PingPongContent("답변 내용"))
        }
    }

    @Test
    fun `핑퐁이 없으면 핑퐁 없음 예외가 발생하고 답변을 저장하지 않는다`() = runTest {
        // given
        coEvery { pingPongRepository.findVisibleById(PingPongId(10L)) } returns null

        // when, then
        assertThrows<PingPongException.PingPongNotFound> {
            usecase(UserId(2L), 10L, "답변 내용")
        }
        coVerify(exactly = 0) {
            pingPongRepository.createAnswer(PingPongId(10L), PingPongContent("답변 내용"))
        }
    }

    @Test
    fun `질문이 달린 게시물을 조회할 수 없으면 게시물 없음 예외가 발생하고 답변을 저장하지 않는다`() = runTest {
        // given
        coEvery { pingPongRepository.findVisibleById(PingPongId(10L)) } returns TestPingPong
        coEvery { userKeywordProvider.findOwnerId(UserId(2L), UserKeywordId(3L)) } returns null

        // when, then
        assertThrows<PingPongException.UserKeywordNotFound> {
            usecase(UserId(2L), 10L, "답변 내용")
        }
        coVerify(exactly = 0) {
            pingPongRepository.createAnswer(PingPongId(10L), PingPongContent("답변 내용"))
        }
    }

    @Test
    fun `게시물 작성자가 아닌 사용자가 답변을 작성하면 예외가 발생하고 답변을 저장하지 않는다`() = runTest {
        // given
        val notOwnerId = UserId(1L)
        coEvery { pingPongRepository.findVisibleById(PingPongId(10L)) } returns TestPingPong
        coEvery { userKeywordProvider.findOwnerId(UserId(1L), UserKeywordId(3L)) } returns UserId(2L)

        // when, then
        assertThrows<PingPongException.NotUserKeywordOwner> {
            usecase(notOwnerId, 10L, "답변 내용")
        }
        coVerify(exactly = 0) {
            pingPongRepository.createAnswer(PingPongId(10L), PingPongContent("답변 내용"))
        }
    }

    @Test
    fun `질문자와 차단 관계이면 차단 관계 예외가 발생하고 답변을 저장하지 않는다`() = runTest {
        // given
        coEvery { pingPongRepository.findVisibleById(PingPongId(10L)) } returns TestPingPong
        coEvery { userKeywordProvider.findOwnerId(UserId(2L), UserKeywordId(3L)) } returns UserId(2L)
        coEvery { blockProvider.isBlockedRelationship(UserId(2L), UserId(1L)) } returns true

        // when, then
        assertThrows<PingPongException.CannotAnswerBlockedQuestioner> {
            usecase(UserId(2L), 10L, "답변 내용")
        }
        coVerify(exactly = 0) {
            pingPongRepository.createAnswer(PingPongId(10L), PingPongContent("답변 내용"))
        }
    }

    @Test
    fun `이미 답변이 등록된 질문이면 답변 중복 예외가 발생한다`() = runTest {
        // given
        coEvery { pingPongRepository.findVisibleById(PingPongId(10L)) } returns TestPingPong
        coEvery { userKeywordProvider.findOwnerId(UserId(2L), UserKeywordId(3L)) } returns UserId(2L)
        coEvery { blockProvider.isBlockedRelationship(UserId(2L), UserId(1L)) } returns false
        coEvery {
            pingPongRepository.createAnswer(PingPongId(10L), PingPongContent("답변 내용"))
        } throws DatabaseException.DuplicatedDataException(SQLException("duplicate key"))

        // when, then
        assertThrows<PingPongException.AlreadyAnswered> {
            usecase(UserId(2L), 10L, "답변 내용")
        }
    }

    @Test
    fun `답변 내용이 유효하지 않으면 유효성 검사 예외가 발생한다`() = runTest {
        // given
        val blankAnswer = " "

        // when, then
        assertThrows<PingPongContentValidationException> {
            usecase(UserId(2L), 10L, blankAnswer)
        }
    }

    @Test
    fun `핑퐁 ID가 0 이하이면 유효성 검사 예외가 발생한다`() = runTest {
        // given
        val invalidPingPongId = 0L

        // when, then
        assertThrows<PingPongIdValidationException> {
            usecase(UserId(2L), invalidPingPongId, "답변 내용")
        }
    }

    @Test
    fun `답변 작성 시 질문자에게 게시물 상세로 이동하는 답변 알림을 전송한다`() = runTest {
        // given
        coEvery { pingPongRepository.findVisibleById(PingPongId(10L)) } returns TestPingPong
        coEvery { userKeywordProvider.findOwnerId(UserId(2L), UserKeywordId(3L)) } returns UserId(2L)
        coEvery { blockProvider.isBlockedRelationship(UserId(2L), UserId(1L)) } returns false
        coEvery { pingPongRepository.createAnswer(PingPongId(10L), PingPongContent("답변 내용")) } returns TestAnswer
        coEvery { userProvider.findUserName(UserId(2L)) } returns UserName("작성자")
        val command = slot<PingPongNotificationCommand>()
        coEvery { notificationProvider.sendNotification(capture(command)) } just Runs

        // when
        usecase(UserId(2L), 10L, "답변 내용")
        applicationScope.advanceUntilIdle()

        // then
        val expected = PingPongNotificationCommand(
            userId = UserId(1L),
            notiType = NotificationType.PING_PONG_ANSWER,
            title = "새 답변",
            message = "작성자 님이 질문에 답변했어요.",
            refId = 3L,
            refType = "KEYWORD",
            refData = mapOf("ref_owner_id" to "2"),
        )
        coVerify(exactly = 1) { notificationProvider.sendNotification(any()) }
        assertEquals(expected, command.captured)
    }

    @Test
    fun `답변자 이름을 조회할 수 없으면 답변 알림을 전송하지 않는다`() = runTest {
        // given
        coEvery { pingPongRepository.findVisibleById(PingPongId(10L)) } returns TestPingPong
        coEvery { userKeywordProvider.findOwnerId(UserId(2L), UserKeywordId(3L)) } returns UserId(2L)
        coEvery { blockProvider.isBlockedRelationship(UserId(2L), UserId(1L)) } returns false
        coEvery { pingPongRepository.createAnswer(PingPongId(10L), PingPongContent("답변 내용")) } returns TestAnswer
        coEvery { userProvider.findUserName(UserId(2L)) } returns null

        // when
        usecase(UserId(2L), 10L, "답변 내용")
        applicationScope.advanceUntilIdle()

        // then
        coVerify(exactly = 0) { notificationProvider.sendNotification(any()) }
    }

    @Test
    fun `이미 답변이 등록된 질문이면 답변 알림을 전송하지 않는다`() = runTest {
        // given
        coEvery { pingPongRepository.findVisibleById(PingPongId(10L)) } returns TestPingPong
        coEvery { userKeywordProvider.findOwnerId(UserId(2L), UserKeywordId(3L)) } returns UserId(2L)
        coEvery { blockProvider.isBlockedRelationship(UserId(2L), UserId(1L)) } returns false
        coEvery {
            pingPongRepository.createAnswer(PingPongId(10L), PingPongContent("답변 내용"))
        } throws DatabaseException.DuplicatedDataException(SQLException("duplicate key"))

        // when
        assertThrows<PingPongException.AlreadyAnswered> {
            usecase(UserId(2L), 10L, "답변 내용")
        }
        applicationScope.advanceUntilIdle()

        // then
        coVerify(exactly = 0) { notificationProvider.sendNotification(any()) }
    }

    companion object {
        private val TestPingPong = PingPong(
            id = PingPongId(10L),
            userKeywordId = UserKeywordId(3L),
            questionerId = UserId(1L),
            question = PingPongContent("질문 내용"),
            createdAt = 1000L,
            updatedAt = 1000L,
        )
        private val TestAnswer = PingPongAnswer(
            id = PingPongAnswerId(20L),
            pingPongId = PingPongId(10L),
            answer = PingPongContent("답변 내용"),
            createdAt = 2000L,
            updatedAt = 2000L,
        )
    }
}
