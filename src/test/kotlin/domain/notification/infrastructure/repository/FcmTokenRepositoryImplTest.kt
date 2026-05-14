package com.turnin.domain.notification.infrastructure.repository

import com.turnin.common.db.schema.UserEntity
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.id.UserId
import com.turnin.util.db.TestDatabaseFactory
import java.time.Instant
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertDoesNotThrow

class FcmTokenRepositoryImplTest {
    private val repository = FcmTokenRepositoryImpl()

    @Before
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @After
    fun teardown() {
        TestDatabaseFactory.cleanUp()
    }

    // ======================== upsert ========================

    @Test
    fun `FCM 토큰 신규 등록 성공`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")

        // when
        val result = repository.upsert(userId, TEST_TOKEN)

        // then
        assertNotNull(result)
        assertEquals(userId.value, result.userId.value)
        assertEquals(TEST_TOKEN, result.token)
        assertTrue(result.isActive)
    }

    @Test
    fun `이미 존재하는 토큰 upsert 시 is_active = true 로 업데이트된다`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        repository.upsert(userId, TEST_TOKEN)

        // 토큰 비활성화
        repository.deactivate(userId, TEST_TOKEN)

        // when: 동일 토큰으로 재등록
        val result = repository.upsert(userId, TEST_TOKEN)

        // then
        assertTrue(result.isActive)
    }

    @Test
    fun `한 사용자가 여러 토큰을 등록할 수 있다`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")

        // when
        repository.upsert(userId, TEST_TOKEN)
        repository.upsert(userId, TEST_TOKEN_2)

        // then
        val tokens = repository.findActiveTokens(userId)
        assertEquals(2, tokens.size)
        assertTrue(tokens.containsAll(listOf(TEST_TOKEN, TEST_TOKEN_2)))
    }

    // ======================== deactivate ========================

    @Test
    fun `FCM 토큰 비활성화 성공`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        repository.upsert(userId, TEST_TOKEN)

        // when
        repository.deactivate(userId, TEST_TOKEN)

        // then
        val tokens = repository.findActiveTokens(userId)
        assertTrue(tokens.isEmpty())
    }

    @Test
    fun `존재하지 않는 토큰 비활성화 시 예외 없이 완료된다`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")

        // when & then
        assertDoesNotThrow { repository.deactivate(userId, TEST_TOKEN) }
    }

    @Test
    fun `다른 사용자의 토큰은 비활성화되지 않는다`() = runTest {
        // given
        val userId1 = insertUserAndReturnId("1")
        val userId2 = insertUserAndReturnId("2")
        repository.upsert(userId1, TEST_TOKEN)

        // when
        repository.deactivate(userId2, TEST_TOKEN)

        // then
        val tokens = repository.findActiveTokens(userId1)
        assertEquals(1, tokens.size) // userId1의 토큰은 그대로
    }

    // ======================== deactivateAll ========================

    @Test
    fun `사용자의 모든 FCM 토큰 비활성화 성공`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        repository.upsert(userId, TEST_TOKEN)
        repository.upsert(userId, TEST_TOKEN_2)

        // when
        repository.deactivateAll(userId)

        // then
        val tokens = repository.findActiveTokens(userId)
        assertTrue(tokens.isEmpty())
    }

    @Test
    fun `다른 사용자의 토큰에는 영향을 주지 않는다`() = runTest {
        // given
        val userId1 = insertUserAndReturnId("1")
        val userId2 = insertUserAndReturnId("2")
        repository.upsert(userId1, TEST_TOKEN)
        repository.upsert(userId2, TEST_TOKEN_2)

        // when: userId1 의 토큰만 전체 비활성화
        repository.deactivateAll(userId1)

        // then: userId2 의 토큰은 그대로
        val tokens = repository.findActiveTokens(userId2)
        assertEquals(1, tokens.size)
    }

    // ======================== deleteAll ========================

    @Test
    fun `사용자의 모든 FCM 토큰 삭제 성공`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        repository.upsert(userId, TEST_TOKEN)
        repository.upsert(userId, TEST_TOKEN_2)

        // when
        repository.deleteAll(userId)

        // then
        val tokens = repository.findActiveTokens(userId)
        assertTrue(tokens.isEmpty())
    }

    // ======================== findActiveTokens ========================

    @Test
    fun `활성화된 토큰만 조회된다`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")
        repository.upsert(userId, TEST_TOKEN)
        repository.upsert(userId, TEST_TOKEN_2)
        repository.deactivate(userId, TEST_TOKEN_2) // TEST_TOKEN_2 비활성화

        // when
        val tokens = repository.findActiveTokens(userId)

        // then
        assertEquals(1, tokens.size)
        assertEquals(TEST_TOKEN, tokens.first())
    }

    @Test
    fun `활성화된 토큰이 없으면 빈 리스트를 반환한다`() = runTest {
        // given
        val userId = insertUserAndReturnId("1")

        // when
        val tokens = repository.findActiveTokens(userId)

        // then
        assertTrue(tokens.isEmpty())
    }

    // ======================== helper ========================

    private suspend fun insertUserAndReturnId(uniqueValue: String): UserId =
        TestDatabaseFactory.dbQuery {
            val savedUser = UserEntity.new {
                this.role = Role.USER
                this.provider = SocialLoginProvider.GOOGLE
                this.providerId = "pid$uniqueValue"
                this.displayId = "did$uniqueValue"
                this.name = "testUser$uniqueValue"
                this.profileImageUrl = null
                this.introduce = "hello"
                this.isActive = true
                this.lastLoginAt = Instant.now()
            }
            UserId(savedUser.id.value)
        }

    companion object {
        private const val TEST_TOKEN = "test_fcm_token_1"
        private const val TEST_TOKEN_2 = "test_fcm_token_2"
    }
}
