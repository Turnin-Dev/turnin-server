package com.turnin.domain.user.application.usecase

import com.turnin.common.model.Introduce
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.UserName
import com.turnin.common.model.id.DisplayId
import com.turnin.common.model.id.UserId
import com.turnin.domain.user.application.dto.toDto
import com.turnin.domain.user.domain.model.User
import com.turnin.domain.user.domain.repository.UserRepository
import com.turnin.util.db.TestDatabaseFactory
import io.mockk.coEvery
import io.mockk.mockk
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Before

class GetUserUseCaseTest {
    private val userRepository = mockk<UserRepository>()
    private val usecase = GetUserUseCase(userRepository)

    @Before
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @AfterTest
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `성공 테스트`() = runTest {
        // given
        coEvery { userRepository.findVisibleById(TestUserId, TestUserId) } returns TestUser

        // when
        val userDto = usecase(TestUserId, TestUserId)

        // then
        assertNotNull(userDto)
        assertEquals(TestUser.toDto(), userDto)
    }

    @Test
    fun `사용자가 존재하지 않을 때 실패 테스트`() = runTest {
        // given
        coEvery { userRepository.findVisibleById(TestUserId, TestUserId) } returns null

        // when
        val userDto = usecase(TestUserId, TestUserId)

        // then
        assertNull(userDto)
    }

    companion object {
        private val TestUserId = UserId(1L)
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
