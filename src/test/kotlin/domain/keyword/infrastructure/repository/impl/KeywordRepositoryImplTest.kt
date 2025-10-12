package com.peekr.domain.keyword.infrastructure.repository.impl

import com.peekr.common.db.DatabaseFactory.dbQuery
import com.peekr.common.db.schema.Role
import com.peekr.common.db.schema.SocialLoginProvider
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.model.KeywordId
import com.peekr.common.model.UserId
import com.peekr.util.TestDatabaseFactory
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull
import org.junit.jupiter.api.assertThrows

class KeywordRepositoryImplTest {
    private val repository = KeywordRepositoryImpl()

    @Before
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @After
    fun teardown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `create 성공 테스트`() = runTest {
        // given
        val userId = insertUserAndReturnId()

        // when
        val keyword = repository.create(TEST_KEYWORD, userId)

        // then
        assertEquals(keyword.keyword, TEST_KEYWORD)
        assertEquals(keyword.createdBy, userId)
    }

    @Test
    fun `create 실패 테스트 - 키워드 길이 제약 위반`() = runTest {
        // given
        val userId = insertUserAndReturnId()

        // when and then
        // 'ValidatorException' 예외가 발생하지 않는 이유:
        // 키워드 테이블의 필드인 키워드 길이 제약이 설정되어있기 때문에
        // Keyword 객체로 만들어지기 이전에 exposed 내부에서 예외를 발생시킨다.
        assertThrows<IllegalArgumentException> {
            repository.create(INVALID_TEST_KEYWORD, userId)
        }
    }

    @Test
    fun `findById 성공 테스트`() = runTest {
        // given
        val userId = insertUserAndReturnId()
        val savedKeyword = repository.create(TEST_KEYWORD, userId)

        // when
        val keyword = repository.findById(savedKeyword.id)

        // then
        assertNotNull(keyword)
        assertEquals(keyword.keyword, savedKeyword.keyword)
        assertEquals(keyword.createdBy, savedKeyword.createdBy)
    }

    @Test
    fun `findById 실패 테스트 - 존재하지 않는 키워드 ID 조회`() = runTest {
        // given
        val invalidKeywordId = KeywordId(0L)

        // when
        val keyword = repository.findById(invalidKeywordId)

        // then
        assertNull(keyword)
    }

    @Test
    fun `findByName 성공 테스트`() = runTest {
        // given
        val userId = insertUserAndReturnId()
        val savedKeyword = repository.create(TEST_KEYWORD, userId)

        // when
        val keyword = repository.findByName(savedKeyword.keyword)

        // then
        assertNotNull(keyword)
        assertEquals(keyword.keyword, savedKeyword.keyword)
        assertEquals(keyword.createdBy, savedKeyword.createdBy)
    }

    @Test
    fun `findByName 실패 테스트 - 존재하지 않는 키워드 ID 조회`() = runTest {
        // given
        val invalidKeywordId = KeywordId(0L)

        // when
        val keyword = repository.findById(invalidKeywordId)

        // then
        assertNull(keyword)
    }

    private suspend fun insertUserAndReturnId(): UserId = dbQuery {
        val savedUser = UserEntity.new {
            this.role = Role.USER
            this.provider = SocialLoginProvider.GOOGLE
            this.providerId = "asdasdads"
            this.displayId = "hong"
            this.name = "honggd"
            this.profileImageUrl = null
            this.introduce = "hello"
            this.isActive = true
            this.lastLoginAt = Instant.now()
        }
        UserId(savedUser.id.value)
    }

    companion object {
        private val INVALID_TEST_KEYWORD = "keyword".repeat(1000)
        private const val TEST_KEYWORD = "keyword"
    }
}
