package com.turnin.domain.pingPong.application.usecase

import com.turnin.common.model.id.PingPongAnswerId
import com.turnin.common.model.id.PingPongId
import com.turnin.common.model.id.PingPongIdValidationException
import com.turnin.common.model.id.UserId
import com.turnin.domain.pingPong.domain.model.PingPongAnswerDeletionTarget
import com.turnin.domain.pingPong.domain.repository.PingPongRepository
import com.turnin.domain.pingPong.exception.PingPongException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertThrows

class DeletePingPongAnswerUseCaseTest {
    private val pingPongRepository = mockk<PingPongRepository>()
    private val usecase = DeletePingPongAnswerUseCase(pingPongRepository)

    @Test
    fun `게시물 작성자가 삭제를 요청하면 조회한 답변 ID로 답변을 삭제한다`() = runTest {
        // given
        val ownerId = UserId(2L)
        coEvery { pingPongRepository.findAnswerDeletionTarget(PingPongId(10L)) } returns PingPongAnswerDeletionTarget(
            ownerId = UserId(2L),
            answerId = PingPongAnswerId(20L),
        )
        coEvery { pingPongRepository.deleteAnswer(PingPongAnswerId(20L)) } returns true

        // when
        usecase(ownerId, 10L)

        // then
        coVerify(exactly = 1) { pingPongRepository.deleteAnswer(PingPongAnswerId(20L)) }
    }

    @Test
    fun `핑퐁이 없으면 핑퐁 없음 예외가 발생하고 삭제하지 않는다`() = runTest {
        // given
        coEvery { pingPongRepository.findAnswerDeletionTarget(PingPongId(10L)) } returns null

        // when, then
        assertThrows<PingPongException.PingPongNotFound> {
            usecase(UserId(2L), 10L)
        }
        coVerify(exactly = 0) { pingPongRepository.deleteAnswer(PingPongAnswerId(20L)) }
    }

    @Test
    fun `게시물 작성자가 아니면 삭제 권한 예외가 발생하고 삭제하지 않는다`() = runTest {
        // given
        val questionerId = UserId(1L)
        coEvery { pingPongRepository.findAnswerDeletionTarget(PingPongId(10L)) } returns PingPongAnswerDeletionTarget(
            ownerId = UserId(2L),
            answerId = PingPongAnswerId(20L),
        )

        // when, then
        assertThrows<PingPongException.NoPermissionToDelete> {
            usecase(questionerId, 10L)
        }
        coVerify(exactly = 0) { pingPongRepository.deleteAnswer(PingPongAnswerId(20L)) }
    }

    @Test
    fun `게시물 또는 작성자가 비활성 상태이면 삭제 권한 예외가 발생하고 삭제하지 않는다`() = runTest {
        // given
        coEvery { pingPongRepository.findAnswerDeletionTarget(PingPongId(10L)) } returns PingPongAnswerDeletionTarget(
            ownerId = null,
            answerId = PingPongAnswerId(20L),
        )

        // when, then
        assertThrows<PingPongException.NoPermissionToDelete> {
            usecase(UserId(2L), 10L)
        }
        coVerify(exactly = 0) { pingPongRepository.deleteAnswer(PingPongAnswerId(20L)) }
    }

    @Test
    fun `답변이 없으면 답변 없음 예외가 발생하고 삭제하지 않는다`() = runTest {
        // given
        coEvery { pingPongRepository.findAnswerDeletionTarget(PingPongId(10L)) } returns PingPongAnswerDeletionTarget(
            ownerId = UserId(2L),
            answerId = null,
        )

        // when, then
        assertThrows<PingPongException.PingPongAnswerNotFound> {
            usecase(UserId(2L), 10L)
        }
        coVerify(exactly = 0) { pingPongRepository.deleteAnswer(PingPongAnswerId(20L)) }
    }

    @Test
    fun `핑퐁 ID가 0 이하이면 유효성 검사 예외가 발생한다`() = runTest {
        // given
        val invalidPingPongId = 0L

        // when, then
        assertThrows<PingPongIdValidationException> {
            usecase(UserId(2L), invalidPingPongId)
        }
    }
}
