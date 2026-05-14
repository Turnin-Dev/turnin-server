package com.turnin.domain.user.application.usecase

import com.turnin.common.model.Introduce
import com.turnin.common.model.IntroduceValidationException
import com.turnin.common.model.id.UserId
import com.turnin.domain.user.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class UpdateIntroduceUseCaseTest {
    private val userRepository: UserRepository = mockk()
    private val usecase = UpdateIntroduceUseCase(userRepository)

    @Test
    fun `소개글 수정 성공 테스트`() = runTest {
        // given
        coEvery {
            userRepository.updateIntroduce(TestUserId, TestIntroduce)
        } returns true

        // when
        val result = usecase(TestUserId, TestIntroduce.value)

        // then
        assertTrue(result)
    }

    @Test
    fun `소개글 수정에 실패하면 false를 반환한다`() = runTest {
        // given
        coEvery {
            userRepository.updateIntroduce(TestUserId, TestIntroduce)
        } returns false

        // when
        val result = usecase(TestUserId, TestIntroduce.value)

        // then
        assertFalse(result)
    }

    @Test
    fun `소개글 유효성 검사 실패 시 에러가 발생한다`() = runTest {
        // given
        val invalidIntroduce = "a".repeat(Introduce.MAX_LENGTH + 1)

        // when, then
        assertFailsWith<IntroduceValidationException> {
            usecase(TestUserId, invalidIntroduce)
        }
    }

    companion object {
        private val TestUserId = UserId(1L)
        private val TestIntroduce = Introduce("test introduce")
    }
}
