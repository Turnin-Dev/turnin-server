package com.peekr.domain.auth.infrastructure.repository.impl

import com.peekr.common.db.schema.RefreshTokens
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.model.UserId
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.util.TestDatabaseFactory
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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
                this.role = MockUser.role
                this.provider = MockUser.provider
                this.providerId = MockUser.providerId
                this.displayId = MockUser.displayId.value
                this.name = MockUser.name.value
                this.profileImageUrl = MockUser.profileImageUrl
                this.introduce = MockUser.introduce?.value
            }

            RefreshTokens.upsert {
                it[user] = savedUserEntity.id
                it[refreshToken] = MOCK_REFRESH_TOKEN
            }

            savedUserEntity.id.value
        }

        // when
        val userId = refreshTokenRepository.findUserIdByRefreshToken(MOCK_REFRESH_TOKEN)

        // then
        assertNotNull(userId)
        assertEquals(UserId(expectedUserId), userId)
    }

    @Test
    fun `findUserIdByRefreshToken 실패 테스트 - 유효하지 않은 토큰으로 조회했을 경우`() = runTest {
        // when
        val userId = refreshTokenRepository.findUserIdByRefreshToken(MOCK_REFRESH_TOKEN)

        // then
        assertNull(userId)
    }

    companion object {
        private const val MOCK_REFRESH_TOKEN = "aaa.bbb.ccc"
        private val MockUser = AuthUser.sample
    }

    @Test
    fun `save 성공 테스트`() = runTest {
        // given
        val userId = TestDatabaseFactory.dbQuery {
            val savedUserEntity = UserEntity.new {
                this.role = MockUser.role
                this.provider = MockUser.provider
                this.providerId = MockUser.providerId
                this.displayId = MockUser.displayId.value
                this.name = MockUser.name.value
                this.profileImageUrl = MockUser.profileImageUrl
                this.introduce = MockUser.introduce?.value
            }
            savedUserEntity.id.value
        }

        // when
        val result = refreshTokenRepository.save(UserId(userId), MOCK_REFRESH_TOKEN)
        val foundUserId = refreshTokenRepository.findUserIdByRefreshToken(MOCK_REFRESH_TOKEN)

        // then
        assertTrue(result)
        assertNotNull(foundUserId)
        assertEquals(UserId(userId), foundUserId)
    }

    @Test
    fun `save 실패 테스트 - 사용자가 존재하지 않는 경우`() = runTest {
        assertFalse {
            refreshTokenRepository.save(UserId(1L), MOCK_REFRESH_TOKEN)
        }
    }
}
