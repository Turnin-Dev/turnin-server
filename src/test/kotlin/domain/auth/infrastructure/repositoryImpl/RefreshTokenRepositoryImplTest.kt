package com.peekr.domain.auth.infrastructure.repositoryImpl

import com.peekr.common.db.scheme.RefreshTokens
import com.peekr.common.db.scheme.UserEntity
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.util.TestDatabaseFactory
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

    @Test
    fun `findNameByRefreshToken 성공 테스트`() = runTest {
        // given
        TestDatabaseFactory.dbQuery {
            val savedUserEntity = UserEntity.new {
                this.provider = MockUser.provider
                this.providerId = MockUser.providerId
                this.name = MockUser.name
                this.nickname = MockUser.nickname
                this.profileImageUrl = MockUser.profileImageUrl
                this.introduce = MockUser.introduce
            }
            val userId = savedUserEntity.id
            RefreshTokens.upsert {
                it[user] = userId
                it[refreshToken] = MOCK_REFRESH_TOKEN
            }
        }

        // when
        val name = refreshTokenRepository.findNameByRefreshToken(MOCK_REFRESH_TOKEN)

        // then
        assertNotNull(name)
        assertEquals(name, MockUser.name)
    }

    @Test
    fun `findNameByRefreshToken 실패 테스트 - 유효하지 않은 토큰으로 조회했을 경우`() = runTest {
        // when
        val name = refreshTokenRepository.findNameByRefreshToken(MOCK_REFRESH_TOKEN)

        // then
        assertNull(name)
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
                this.provider = MockUser.provider
                this.providerId = MockUser.providerId
                this.name = MockUser.name
                this.nickname = MockUser.nickname
                this.profileImageUrl = MockUser.profileImageUrl
                this.introduce = MockUser.introduce
            }
            savedUserEntity.id.value
        }

        // when
        val result = refreshTokenRepository.save(userId, MOCK_REFRESH_TOKEN)
        val name = refreshTokenRepository.findNameByRefreshToken(MOCK_REFRESH_TOKEN)

        // then
        assertTrue(result)
        assertNotNull(name)
        assertEquals(name, MockUser.name)
    }

    @Test
    fun `save 실패 테스트 - 사용자가 존재하지 않는 경우`() = runTest {
        assertFalse {
            refreshTokenRepository.save(1L, MOCK_REFRESH_TOKEN)
        }
    }
}
