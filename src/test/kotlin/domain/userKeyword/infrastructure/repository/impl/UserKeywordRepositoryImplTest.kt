package com.peekr.domain.userKeyword.infrastructure.repository.impl

import com.peekr.common.db.DatabaseException
import com.peekr.common.db.schema.KeywordEntity
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.db.schema.Users
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.userKeyword.application.dto.DescriptionDto
import com.peekr.domain.userKeyword.application.dto.toDomain
import com.peekr.domain.userKeyword.domain.model.Description
import com.peekr.util.TestDatabaseFactory
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.jetbrains.exposed.dao.id.EntityID
import org.junit.After
import org.junit.Before
import org.junit.jupiter.api.assertThrows

class UserKeywordRepositoryImplTest {
    private val repository = UserKeywordRepositoryImpl()

    @Before
    fun setUp() {
        TestDatabaseFactory.init()
    }

    @After
    fun teardown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `findById 성공 테스트`() = runTest {
        // given
        val userId = insertUserAndReturnId()
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )

        // when
        val actualUserKeyword = repository.findById(userKeyword.id)

        // then
        assertNotNull(actualUserKeyword)
        assertEquals(userKeyword, actualUserKeyword)
    }

    @Test
    fun `findById 실패 테스트 - 데이터가 없으면 null을 반환한다`() = runTest {
        // when
        val actualUserKeyword = repository.findById(UserKeywordId(1L))

        // then
        assertNull(actualUserKeyword)
    }

    @Test
    fun `findListByUserId 성공 테스트`() = runTest {
        // given
        val userId = insertUserAndReturnId()
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )

        // when
        val userKeywords = repository.findListByUserId(userId)

        // then
        assertEquals(1, userKeywords.size)
        assertEquals(userKeywords.first().id, userKeyword.id)
        assertEquals(userKeywords.first().keywordId, keywordId)
    }

    @Test
    fun `findListByUserId 성공 테스트 - 키워드 설명 필드가 길면 텍스트 일부만 가져온다`() = runTest {
        // given
        val expectedDescriptionLength = 300
        val userId = insertUserAndReturnId()
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        repository.create(
            keywordId = keywordId,
            userId = userId,
            description = Description("a".repeat(expectedDescriptionLength)),
        )

        // when
        val userKeywords = repository.findListByUserId(userId)

        // then
        assertEquals(1, userKeywords.size)
        assertNotEquals(
            expectedDescriptionLength,
            userKeywords
                .first()
                .description.value
                ?.length,
        )
    }

    @Test
    fun `findListByUserId 성공 테스트 - 등록된 키워드가 없는 상태에서 조회 시 빈 리스트를 반환한다`() = runTest {
        // given
        val userId = insertUserAndReturnId()

        // when
        val userKeywords = repository.findListByUserId(userId)

        // then
        assertTrue(userKeywords.isEmpty())
    }

    @Test
    fun `findListByUserId 실패 테스트 - 존재하지 않는 사용자의 사용자 키워드 조회 시 빈 리스트를 반환한다`() = runTest {
        val userKeywords = repository.findListByUserId(UserId(10))

        assertTrue(userKeywords.isEmpty())
    }

    @Test
    fun `create 성공 테스트`() = runTest {
        // given
        val userId = insertUserAndReturnId()
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)

        // when
        val savedUserKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )

        // then
        assertEquals(savedUserKeyword.keywordId, keywordId)
        assertEquals(savedUserKeyword.userId, userId)
    }

    @Test
    fun `create 실패 테스트 - 외래키 제약 위반 발생 시 알려진 예외가 발생한다`() = runTest {
        assertThrows<DatabaseException.ForeignKeyViolationException> {
            repository.create(
                keywordId = KeywordId(1),
                userId = UserId(1),
                description = TestDescription,
            )
        }
    }

    @Test
    fun `findByKeywordIdAndUserId 성공 테스트`() = runTest {
        // given
        val userId = insertUserAndReturnId()
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )

        // when
        val actualUserKeyword = repository.findByKeywordIdAndUserId(keywordId, userId)

        // then
        assertNotNull(actualUserKeyword)
        assertEquals(userKeyword, actualUserKeyword)
    }

    @Test
    fun `findByKeywordIdAndUserId 실패 테스트 - 존재하지 않는 사용자의 사용자 키워드 조회 시 null을 반환한다`() = runTest {
        val actualUserKeyword = repository.findByKeywordIdAndUserId(KeywordId(10), UserId(10))

        assertNull(actualUserKeyword)
    }

    @Test
    fun `updateDescription 성공 테스트`() = runTest {
        // given
        val userId = insertUserAndReturnId()
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )
        val patch = DescriptionDto(value = "hello")

        // when
        val result = repository.updateDescription(userId, userKeyword.id, patch.toDomain())
        val patchedUserKeyword = repository.findByKeywordIdAndUserId(keywordId, userId)

        // then
        assertTrue(result)
        assertNotNull(patchedUserKeyword)
    }

    @Test
    fun `updateDescription 실패 테스트 - 존재하지 않는 사용자 ID 혹은 사용자 키워드 ID 조회 시 false 반환`() = runTest {
        val patch = DescriptionDto(value = "hello")
        val result =
            repository.updateDescription(UserId(10), UserKeywordId(10), patch.toDomain())

        assertFalse(result)
    }

    @Test
    fun `delete 성공 테스트`() = runTest {
        // given
        val userId = insertUserAndReturnId()
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )

        // when
        val result = repository.delete(userId, userKeyword.id)

        // then
        assertTrue(result)
    }

    @Test
    fun `delete 실패 테스트 - 존재하지 않는 사용자 ID 혹은 사용자 키워드 ID 조회 시 false 반환`() = runTest {
        val result = repository.delete(UserId(10), UserKeywordId(10))

        assertFalse(result)
    }

    @Test
    fun `findDescriptionById 성공 테스트`() = runTest {
        // given
        val userId = insertUserAndReturnId()
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = TestDescription,
        )

        // when
        val description = repository.findDescriptionById(userId, userKeyword.id)

        // then
        assertNotNull(description)
        assertEquals(description, TestDescription)
    }

    @Test
    fun `findDescriptionById 성공 테스트 - 등록되지 않은 사용자 키워드 조회 시 null을 반환한다`() = runTest {
        // given
        val userId = insertUserAndReturnId()

        // when
        val description = repository.findDescriptionById(userId, UserKeywordId(1L))

        // then
        assertNull(description)
    }

    @Test
    fun `findDescriptionById 성공 테스트 - 사용자 키워드 설명이 비어있는 경우 null을 반환한다`() = runTest {
        // given
        val userId = insertUserAndReturnId()
        val keywordId = insertKeywordAndReturnId(userId, TEST_KEYWORD)
        val userKeyword = repository.create(
            keywordId = keywordId,
            userId = userId,
            description = Description(null),
        )

        // when
        val description = repository.findDescriptionById(userId, userKeyword.id)

        // then
        assertNull(description)
    }

    private suspend fun insertUserAndReturnId(): UserId = TestDatabaseFactory.dbQuery {
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

    private suspend fun insertKeywordAndReturnId(
        userId: UserId,
        keyword: String,
    ): KeywordId = TestDatabaseFactory.dbQuery {
        val savedKeyword = KeywordEntity.new {
            this.keyword = keyword
            this.createdBy = EntityID(userId.value, Users)
        }
        KeywordId(savedKeyword.id.value)
    }

    companion object {
        private val TestDescription = Description("hello")
        private const val TEST_KEYWORD = "keyword"
    }
}
