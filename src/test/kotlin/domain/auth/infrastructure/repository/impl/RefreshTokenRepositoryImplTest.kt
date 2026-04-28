package com.turnin.domain.auth.infrastructure.repository.impl

import com.turnin.common.db.DatabaseException
import com.turnin.common.db.schema.RefreshTokens
import com.turnin.common.db.schema.UserEntity
import com.turnin.common.model.Introduce
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.UserName
import com.turnin.common.model.id.DisplayId
import com.turnin.common.model.id.UserId
import com.turnin.domain.auth.domain.model.AuthUser
import com.turnin.util.db.TestDatabaseFactory
import com.turnin.util.db.setUserInactiveForTest
import java.time.Instant
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.sql.upsert
import org.junit.Before

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
                this.name = TestAuthUser.userName.value
                this.profileImageUrl = TestAuthUser.profileImageUrl
                this.introduce = TestAuthUser.introduce.value
                this.lastLoginAt = Instant.now()
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
    fun `findUserIdByRefreshToken 성공 테스트 - 비활성화 사용자는 조회되지 않는다`() = runTest {
        // given: 사용자 생성 후 비활성화
        val expectedUserId = TestDatabaseFactory.dbQuery {
            val savedUserEntity = UserEntity.new {
                this.role = TestAuthUser.role
                this.provider = TestAuthUser.provider
                this.providerId = TestAuthUser.providerId
                this.displayId = TestAuthUser.displayId.value
                this.name = TestAuthUser.userName.value
                this.profileImageUrl = TestAuthUser.profileImageUrl
                this.introduce = TestAuthUser.introduce.value
                this.lastLoginAt = Instant.now()
            }

            RefreshTokens.upsert {
                it[user] = savedUserEntity.id
                it[refreshToken] = TEST_REFRESH_TOKEN
            }

            savedUserEntity.id.value
        }
        setUserInactiveForTest(UserId(expectedUserId))

        // when: 비활성화 사용자 토큰 조회
        val userId = refreshTokenRepository.findUserIdByRefreshToken(TEST_REFRESH_TOKEN)

        // then: 조회되지 않는다.
        assertNull(userId)
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
                this.name = TestAuthUser.userName.value
                this.profileImageUrl = TestAuthUser.profileImageUrl
                this.introduce = TestAuthUser.introduce.value
                this.lastLoginAt = Instant.now()
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
        assertFailsWith<DatabaseException.ForeignKeyViolationException> {
            refreshTokenRepository.save(UserId(1L), TEST_REFRESH_TOKEN)
        }
    }

    @Test
    fun `delete 성공 테스트`() = runTest {
        // given
        val userId = TestDatabaseFactory.dbQuery {
            val savedUserEntity = UserEntity.new {
                this.role = TestAuthUser.role
                this.provider = TestAuthUser.provider
                this.providerId = TestAuthUser.providerId
                this.displayId = TestAuthUser.displayId.value
                this.name = TestAuthUser.userName.value
                this.profileImageUrl = TestAuthUser.profileImageUrl
                this.introduce = TestAuthUser.introduce.value
                this.lastLoginAt = Instant.now()
            }

            RefreshTokens.upsert {
                it[user] = savedUserEntity.id
                it[refreshToken] = TEST_REFRESH_TOKEN
            }

            savedUserEntity.id.value
        }
        val foundedUserId = refreshTokenRepository.findUserIdByRefreshToken(TEST_REFRESH_TOKEN)
        assertNotNull(foundedUserId)
        assertEquals(UserId(userId), foundedUserId)

        // when: 토큰 삭제
        refreshTokenRepository.delete(foundedUserId)

        // then: 토큰이 없는지 검증
        assertNull(refreshTokenRepository.findUserIdByRefreshToken(TEST_REFRESH_TOKEN))
    }

    companion object {
        private const val TEST_REFRESH_TOKEN = "aaa.bbb.ccc"
        private val TestAuthUser = AuthUser(
            userId = UserId(1L),
            role = Role.USER,
            provider = SocialLoginProvider.GOOGLE,
            providerId = "123123123",
            displayId = DisplayId("hong_gd_123"),
            userName = UserName("honggd"),
            profileImageUrl = "https://example.com/image.jpg",
            introduce = Introduce("hello world!"),
            isActive = true,
            lastLoginAt = Instant.ofEpochMilli(1697875200000L),
        )
    }
}
