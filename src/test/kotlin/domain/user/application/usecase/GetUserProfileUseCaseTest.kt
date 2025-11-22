package com.peekr.domain.user.application.usecase

import com.peekr.common.model.Introduce
import com.peekr.common.model.Name
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.UserId
import com.peekr.domain.user.application.dto.toDto
import com.peekr.domain.user.domain.model.User
import com.peekr.domain.user.domain.model.UserProfile
import com.peekr.domain.user.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class GetUserProfileUseCaseTest {
    private val userRepository: UserRepository = mockk()
    private val usecase = GetUserProfileUseCase(userRepository)

    @Test
    fun `사용자 프로필 조회 성공 테스트`() = runTest {
        // given
        coEvery {
            userRepository.findUserProfileById(TestUserId)
        } returns TestUserProfile

        // when
        val userProfileDto = usecase(TestUserId)

        // then
        assertEquals(TestUserProfile.toDto(), userProfileDto)
    }

    @Test
    fun `사용자 프로필 조회 실패 시 null을 반환한다`() = runTest {
        // given
        coEvery {
            userRepository.findUserProfileById(TestUserId)
        } returns null

        // when
        val userProfileDto = usecase(TestUserId)

        // then
        assertNull(userProfileDto)
    }

    companion object {
        private val TestUserId = UserId(1L)
        private val TestUserProfile = UserProfile(
            user = User(
                id = TestUserId,
                role = Role.USER,
                provider = SocialLoginProvider.GOOGLE,
                providerId = "providerId",
                displayId = DisplayId("displayId"),
                name = Name("name"),
                profileImageUrl = "profileImageUrl",
                introduce = Introduce("introduce"),
                isActive = true,
                lastLoginAt = Instant.now(),
            ),
            friendsCount = 10,
        )
    }
}
