package com.peekr.domain.user.application.usecase

import com.peekr.common.model.Introduce
import com.peekr.common.model.UserId
import com.peekr.domain.user.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
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

    companion object {
        private val TestUserId = UserId(1L)
        private val TestIntroduce = Introduce("test introduce")
    }
}
