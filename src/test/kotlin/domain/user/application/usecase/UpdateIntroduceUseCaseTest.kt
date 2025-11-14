package com.peekr.domain.user.application.usecase

import com.peekr.common.model.UserId
import com.peekr.domain.user.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlinx.coroutines.test.runTest

class UpdateIntroduceUseCaseTest {
    private val userRepository: UserRepository = mockk()
    private val usecase = UpdateIntroduceUseCase(userRepository)

    @Test
    fun `소개글 수정 성공 테스트`() = runTest {
        // given
        coEvery {
            userRepository.updateIntroduce(TestUserId, TEST_INTRODUCE)
        } returns true

        // when
        val result = usecase(TestUserId, TEST_INTRODUCE)

        // then
        assert(result)
    }

    @Test
    fun `소개글 수정에 실패하면 false를 반환한다`() = runTest {
        // given
        coEvery {
            userRepository.updateIntroduce(TestUserId, TEST_INTRODUCE)
        } returns false

        // when
        val result = usecase(TestUserId, TEST_INTRODUCE)

        // then
        assertFalse(result)
    }

    companion object {
        private val TestUserId = UserId(1L)
        private const val TEST_INTRODUCE = "test introduce"
    }
}
