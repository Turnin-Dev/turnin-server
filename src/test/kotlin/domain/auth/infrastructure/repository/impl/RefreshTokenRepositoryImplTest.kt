package com.peekr.domain.auth.infrastructure.repository.impl

import com.peekr.common.db.DatabaseException
import com.peekr.common.db.schema.RefreshTokens
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.model.DisplayId
import com.peekr.common.model.Introduce
import com.peekr.common.model.Name
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.UserId
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.util.TestDatabaseFactory
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.upsert
import org.junit.Before
import org.junit.jupiter.api.assertThrows

class RefreshTokenRepositoryImplTest {
    private val refreshTokenRepository = RefreshTokenRepositoryImpl()

    @Before
    fun setUp() = runTest {
        TestDatabaseFactory.init()
    }

    @AfterTest
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `findUserIdByRefreshToken 성공 테스트`() = runTest {
        // given
        val expectedUserId = TestDatabaseFactory.dbQuery {
            val savedUserEntity = UserEntity.new {
                this.role = TestAuthUser.role
                this.provider = TestAuthUser.provider
                this.providerId = TestAuthUser.providerId
                this.displayId = TestAuthUser.displayId.value
                this.name = TestAuthUser.name.value
                this.profileImageUrl = TestAuthUser.profileImageUrl
                this.introduce = TestAuthUser.introduce?.value
            }

            RefreshTokens.upsert {
                it[user] = savedUserEntity.id
                it[refreshToken] = TEST_REFRESH_TOKEN
            }

            savedUserEntity.id.value
        }

        // when
        val userId = refreshTokenRepository.findUserIdByRefreshToken(TEST_REFRESH_TOKEN)

        // then
        assertNotNull(userId)
        assertEquals(UserId(expectedUserId), userId)
    }

    @Test
    fun `findUserIdByRefreshToken 실패 테스트 - 유효하지 않은 토큰으로 조회했을 경우`() = runTest {
        // when
        val userId = refreshTokenRepository.findUserIdByRefreshToken(TEST_REFRESH_TOKEN)

        // then
        assertNull(userId)
    }

    @Test
    fun `save 성공 테스트`() = runTest {
        // given
        val userId = TestDatabaseFactory.dbQuery {
            val savedUserEntity = UserEntity.new {
                this.role = TestAuthUser.role
                this.provider = TestAuthUser.provider
                this.providerId = TestAuthUser.providerId
                this.displayId = TestAuthUser.displayId.value
                this.name = TestAuthUser.name.value
                this.profileImageUrl = TestAuthUser.profileImageUrl
                this.introduce = TestAuthUser.introduce?.value
            }
            savedUserEntity.id.value
        }

        // when
        val result = refreshTokenRepository.save(UserId(userId), TEST_REFRESH_TOKEN)
        val foundUserId = refreshTokenRepository.findUserIdByRefreshToken(TEST_REFRESH_TOKEN)

        // then
        assertTrue(result)
        assertNotNull(foundUserId)
        assertEquals(UserId(userId), foundUserId)
    }

    @Test
    fun `save 실패 테스트 - 사용자가 존재하지 않는 경우`() = runTest {
        assertThrows<DatabaseException.ForeignKeyViolationException> {
            refreshTokenRepository.save(UserId(1L), TEST_REFRESH_TOKEN)
        }
    }

    companion object {
        private const val TEST_REFRESH_TOKEN = "aaa.bbb.ccc"
        private val TestAuthUser = AuthUser(
            userId = UserId(1L),
            role = Role.USER,
            provider = SocialLoginProvider.GOOGLE,
            providerId = "123123123",
            displayId = DisplayId("hong_gd_123"),
            name = Name("honggd"),
            profileImageUrl = "https://example.com/image.jpg",
            introduce = Introduce("hello world!"),
            isActive = true,
            lastLoginAt = Instant.ofEpochMilli(1697875200000L),
        )
    }
}
