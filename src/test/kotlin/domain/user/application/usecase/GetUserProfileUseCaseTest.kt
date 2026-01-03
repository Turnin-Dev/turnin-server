package com.peekr.domain.user.application.usecase

import com.peekr.common.model.FriendStatus
import com.peekr.common.model.Introduce
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.UserName
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.UserId
import com.peekr.domain.user.domain.model.User
import com.peekr.domain.user.domain.provider.FriendProvider
import com.peekr.domain.user.domain.repository.UserRepository
import io.mockk.coEvery
import io.mockk.mockk
import java.time.Instant
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class GetUserProfileUseCaseTest {
    private val userRepository: UserRepository = mockk()
    private val friendProvider: FriendProvider = mockk()
    private val usecase = GetUserProfileUseCase(userRepository, friendProvider)

    @BeforeTest
    fun setUp() {
        coEvery { userRepository.findById(TestUserId) } returns TestUser
        coEvery { friendProvider.countFriends(TestUserId) } returns 10L
        coEvery {
            friendProvider.getFriendStatus(TestMyUserId, TestUserId)
        } returns FriendStatus.FRIENDS
    }

    @Test
    fun `사용자 프로필 조회 성공 테스트`() = runTest {
        val userProfileDto = usecase(TestMyUserId, TestUserId.value)

        assertNotNull(userProfileDto)
        assertEquals(TestUser.displayId, userProfileDto.displayId)
        assertEquals(TestUser.userName, userProfileDto.userName)
    }

    @Test
    fun `사용자를 찾지 못하는 경우 null을 반환한다`() = runTest {
        // given
        coEvery { userRepository.findById(TestUserId) } returns null

        // when
        val userProfileDto = usecase(TestMyUserId, TestUserId.value)

        // then
        assertNull(userProfileDto)
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
