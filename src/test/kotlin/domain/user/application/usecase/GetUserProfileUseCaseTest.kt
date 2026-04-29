package com.turnin.domain.user.application.usecase

import com.turnin.common.model.FriendStatus
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
import io.mockk.coVerify
import io.mockk.mockk
import java.time.Instant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class GetUserProfileUseCaseTest {
    private val userRepository: UserRepository = mockk()
    private val friendProvider: FriendProvider = mockk()
    private val usecase = GetUserProfileUseCase(userRepository, friendProvider)

    @BeforeTest
    fun setUp() {
        coEvery { userRepository.findVisibleById(TestMyUserId, TestUserId) } returns TestUser
        coEvery { userRepository.findActiveById(TestUserId) } returns TestUser
        coEvery { friendProvider.countFriends(TestUserId) } returns 10L
        coEvery {
            friendProvider.getFriendStatus(TestMyUserId, TestUserId)
        } returns FriendStatus.FRIENDS
    }

    @Test
    fun `사용자 프로필 조회 성공 테스트`() = runTest {
        val userProfileDto = usecase(TestMyUserId.value, TestUserId.value)

        assertNotNull(userProfileDto)
        assertEquals(TestUser.displayId, userProfileDto.displayId)
        assertEquals(TestUser.userName, userProfileDto.userName)
    }

    @Test
    fun `사용자를 찾지 못하는 경우 null을 반환한다`() = runTest {
        // given
        coEvery { userRepository.findVisibleById(TestMyUserId, TestUserId) } returns null
        coEvery { userRepository.findActiveById(TestUserId) } returns null

        // when
        val userProfileDto = usecase(TestMyUserId.value, TestUserId.value)

        // then
        assertNull(userProfileDto)
    }

    @Test
    fun `차단 여부가 true인 사용자 조회 시 마스킹된 데이터를 반환한다`() = runTest {
        // given
        coEvery {
            userRepository.findVisibleById(TestMyUserId, TestUserId)
        } returns TestUser.copy(isBlocked = true)

        // when
        val userProfileDto = usecase(TestMyUserId.value, TestUserId.value)

        // then
        assertNotNull(userProfileDto)
        assertTrue(userProfileDto.isBlocked)
        assertEquals(0, userProfileDto.friendsCount)
        assertEquals(FriendStatus.NOTHING, userProfileDto.friendStatus)
        coVerify(exactly = 0) { friendProvider.countFriends(TestUserId) }
        coVerify(exactly = 0) { friendProvider.getFriendStatus(TestMyUserId, TestUserId) }
    }

    companion object {
        private val TestMyUserId = UserId(1L)
        private val TestUserId = UserId(2L)
        private val TestDisplayId = DisplayId("displayId")
        private val TestUser = User(
            id = TestUserId,
            role = Role.USER,
            provider = SocialLoginProvider.GOOGLE,
            providerId = "providerId",
            displayId = TestDisplayId,
            userName = UserName("name"),
            profileImageUrl = "profileImageUrl",
            introduce = Introduce("introduce"),
            isActive = true,
            lastLoginAt = Instant.now(),
        )
    }
}
