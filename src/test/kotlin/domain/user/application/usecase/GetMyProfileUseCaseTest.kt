package com.turnin.domain.user.application.usecase

import com.turnin.common.model.Introduce
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.UserName
import com.turnin.common.model.id.DisplayId
import com.turnin.common.model.id.UserId
import com.turnin.domain.user.domain.model.User
import com.turnin.domain.user.domain.provider.FriendProvider
import com.turnin.domain.user.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class GetMyProfileUseCaseTest {
    private val userRepository: UserRepository = mockk()
    private val friendProvider: FriendProvider = mockk()
    private val usecase = GetMyProfileUseCase(userRepository, friendProvider)

    @Test
    fun `나의 프로필 조회 성공 테스트`() = runTest {
        // given
        coEvery { userRepository.findVisibleById(TestUserId, TestUserId) } returns TestUser
        coEvery { friendProvider.countFriends(TestUserId) } returns TEST_FRIENDS_COUNT

        // when
        val userProfileDto = usecase(TestUserId)

        // then
        assertNotNull(userProfileDto)
        assertEquals(TestUser.displayId, userProfileDto.displayId)
        assertEquals(TEST_FRIENDS_COUNT, userProfileDto.friendsCount)
    }

    @Test
    fun `나의 프로필 조회 실패 시 null을 반환한다`() = runTest {
        // given
        coEvery { userRepository.findVisibleById(TestUserId, TestUserId) } returns null

        // when
        val userProfileDto = usecase(TestUserId)

        // then
        assertNull(userProfileDto)
    }

    companion object {
        private val TestUserId = UserId(1L)
        private const val TEST_FRIENDS_COUNT = 10L
        private val TestUser = User(
            id = TestUserId,
            role = Role.USER,
            provider = SocialLoginProvider.GOOGLE,
            providerId = "providerId",
            displayId = DisplayId("displayId"),
            userName = UserName("name"),
            profileImageUrl = "profileImageUrl",
            introduce = Introduce("introduce"),
            isActive = true,
            lastLoginAt = Instant.now(),
        )
    }
}
