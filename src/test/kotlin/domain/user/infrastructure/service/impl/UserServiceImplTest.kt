package com.peekr.domain.user.infrastructure.service.impl

import com.peekr.common.model.DisplayId
import com.peekr.common.model.Name
import com.peekr.common.model.UserId
import com.peekr.domain.user.UserTestDoubles
import com.peekr.domain.user.domain.model.RoleForUser
import com.peekr.domain.user.domain.model.SocialLoginProviderForUser
import com.peekr.domain.user.domain.model.User
import com.peekr.domain.user.domain.model.UserPatch
import com.peekr.domain.user.domain.model.UserProfile
import com.peekr.domain.user.domain.repository.UserRepository
import com.peekr.domain.user.infrastructure.mapper.UserMapper
import com.peekr.util.TestDatabaseFactory
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.jupiter.api.assertNull

class UserServiceImplTest {
    private val repository = mockk<UserRepository>()
    private val service = UserServiceImpl(repository)

    @Before
    fun setup() {
        TestDatabaseFactory.init()
    }

    @AfterTest
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `getUserById 성공 테스트`() = runBlocking {
        // given
        val mappedUser = TestDatabaseFactory.dbQuery {
            val userEntity = UserTestDoubles.saveAndGetUserEntity()
            UserMapper.toDomain(userEntity)
        }
        coEvery { repository.findById(mappedUser.id) } returns mappedUser

        // when
        val user = service.getUserById(mappedUser.id)

        // then
        assertNotNull(user)
        assertEquals(mappedUser.id, user.id)
    }

    @Test
    fun `getUserById 실패 테스트 - 사용자가 존재하지 않는 경우 null을 반환한다`() = runBlocking {
        // given
        coEvery { repository.findById(UserId(1L)) } returns null

        // when
        val user = service.getUserById(UserId(1L))

        // then
        assertNull(user)
    }

    @Test
    fun `getUserProfileById 성공 테스트`() = runTest {
        // given
        coEvery {
            repository.findUserProfileById(TestUserId)
        } returns TestUserProfile

        // when
        val result = service.getUserProfileById(TestUserId)

        // then
        assertNotNull(result)
        assertEquals(TestUserProfile.user, result.user)
        assertEquals(TestUserProfile.friendsCount, result.friendsCount)
    }

    @Test
    fun `getUserProfileById 실패 테스트 - 사용자가 존재하지 않는 경우 null을 반환한다`() = runTest {
        // given
        coEvery {
            repository.findUserProfileById(TestUserId)
        } returns null

        // when
        val result = service.getUserProfileById(TestUserId)

        // then
        assertNull(result)
    }

    @Test
    fun `updateUser 성공 테스트`() = runTest {
        // given
        coEvery {
            repository.update(TestUserId, TestUserPatch)
        } returns true

        // when
        val result = service.updateUser(TestUserId, TestUserPatch)

        // then
        assertTrue(result)
    }

    @Test
    fun `updateUser 실패 테스트 - 사용자가 존재하지 않는 경우 false를 반환한다`() = runTest {
        // given
        coEvery {
            repository.update(TestUserId, TestUserPatch)
        } returns false

        // when
        val result = service.updateUser(TestUserId, TestUserPatch)

        // then
        assertFalse(result)
    }

    companion object {
        private val TestUserId = UserId(1L)
        private val TestUserPatch = UserPatch(
            displayId = DisplayId("ididid"),
            name = Name("name"),
            profileImageUrl = null,
            introduce = "introduce",
        )
        private val TestUserProfile = UserProfile(
            user = User(
                id = UserId(1L),
                role = RoleForUser.USER,
                provider = SocialLoginProviderForUser.GOOGLE,
                providerId = "123901239",
                displayId = DisplayId("hong_gd_123"),
                name = Name("honggd"),
                profileImageUrl = "https://example.com/image.jpg",
                introduce = "hello world!",
                isActive = true,
                lastLoginAt = null,
            ),
            friendsCount = 2,
        )
    }
}
