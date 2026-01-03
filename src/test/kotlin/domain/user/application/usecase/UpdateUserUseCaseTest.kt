package com.peekr.domain.user.application.usecase

import com.peekr.common.model.Introduce
import com.peekr.common.model.UserName
import com.peekr.common.model.id.UserId
import com.peekr.domain.user.application.dto.UserPatchDto
import com.peekr.domain.user.domain.model.UserPatch
import com.peekr.domain.user.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class UpdateUserUseCaseTest {
    private val userRepository = mockk<UserRepository>()
    private val usecase = UpdateUserUseCase(userRepository)

    @Test
    fun `성공 테스트`() = runTest {
        // given
        coEvery {
            userRepository.update(TestUserId, TestUserPatch)
        } returns true

        // when
        val result = usecase(TestUserId, TestUserPatchDto)

        // then
        assertTrue(result)
    }

    companion object {
        private val TestUserId = UserId(1L)
        private val TestUserPatchDto = UserPatchDto(
            userName = UserName("name"),
            profileImageUrl = null,
            introduce = Introduce("introduce"),
        )
        private val TestUserPatch = UserPatch(
            userName = UserName("name"),
            profileImageUrl = null,
            introduce = Introduce("introduce"),
        )
    }
}
