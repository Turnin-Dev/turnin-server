package com.peekr.domain.user.application.usecase

import com.peekr.common.model.DisplayId
import com.peekr.common.model.Name
import com.peekr.common.model.UserId
import com.peekr.domain.user.application.dto.UserPatchDto
import com.peekr.domain.user.domain.model.UserPatch
import com.peekr.domain.user.domain.service.UserService
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class UpdateUserUseCaseTest {
    private val userService = mockk<UserService>()
    private val usecase = UpdateUserUseCase(userService)

    @Test
    fun `성공 테스트`() = runTest {
        // given
        coEvery {
            userService.updateUser(TestUserId, TestUserPatch)
        } returns true

        // when
        val result = usecase(TestUserId, TestUserPatchDto)

        // then
        assertTrue(result)
    }

    companion object {
        private val TestUserId = UserId(1L)
        private val TestUserPatchDto = UserPatchDto(
            displayId = DisplayId("ididid"),
            name = Name("name"),
            profileImageUrl = null,
            introduce = "introduce",
        )
        private val TestUserPatch = UserPatch(
            displayId = DisplayId("ididid"),
            name = Name("name"),
            profileImageUrl = null,
            introduce = "introduce",
        )
    }
}
