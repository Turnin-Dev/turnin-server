package com.turnin.domain.keyword.infrastructure.repository.impl

import com.turnin.common.db.schema.UserEntity
import com.turnin.common.ml.keywordCategory.KeywordCategory
import com.turnin.common.model.KeywordName
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.util.db.TestDatabaseFactory
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.jupiter.api.assertNotNull
import org.junit.jupiter.api.assertNull

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
        val keyword = repository.create(
            TestKeywordName,
            TEST_EMBEDDED_KEYWORD,
            TestCategory,
            TEST_CATEGORY_SIMILARITY,
            userId,
        )

        // then
        assertEquals(TestKeywordName, keyword.name)
        assertEquals(userId, keyword.createdBy)
        assertEquals(TestCategory, keyword.category)
        assertEquals(TEST_CATEGORY_SIMILARITY.toDouble(), keyword.categorySimilarity)
    }

    @Test
    fun `create 성공 테스트 - 미분류 키워드는 category와 categorySimilarity가 null이다`() = runTest {
        // given
        val userId = insertUserAndReturnId()

        // when
        val keyword = repository.create(
            TestKeywordName,
            TEST_EMBEDDED_KEYWORD,
            null,
            null,
            userId,
        )

        // then
        assertNull(keyword.category)
        assertNull(keyword.categorySimilarity)
    }

    @Test
    fun `findById 성공 테스트`() = runTest {
        // given
        val userId = insertUserAndReturnId()
        val savedKeyword = repository.create(
            TestKeywordName,
            TEST_EMBEDDED_KEYWORD,
            null,
            null,
            userId,
        )

        // when
        val keyword = repository.findById(savedKeyword.id)

        // then
        assertNotNull(keyword)
        assertEquals(keyword.name, savedKeyword.name)
        assertEquals(keyword.createdBy, savedKeyword.createdBy)
    }

    @Test
    fun `findById 실패 테스트 - 존재하지 않는 키워드 ID 조회`() = runTest {
        // given
        val invalidKeywordId = KeywordId(1L)

        // when
        val keyword = repository.findById(invalidKeywordId)

        // then
        assertNull(keyword)
    }

    @Test
    fun `findByIds 성공 테스트`() = runTest {
        // given
        val userId = insertUserAndReturnId()
        val expectedKeywords = List(5) {
            repository.create(
                KeywordName(it.toString()),
                TEST_EMBEDDED_KEYWORD,
                null,
                null,
                userId,
            )
        }

        // when
        val actualKeywords = repository.findByIds(expectedKeywords.map { it.id })

        // then
        assertEquals(expectedKeywords.size, actualKeywords.size)
        assertEquals(expectedKeywords, actualKeywords)
    }

    @Test
    fun `findByIds 실패 테스트 - 빈 리스트를 입력 시 빈 리스트를 즉시 반환한다`() = runTest {
        // when
        val actualKeywords = repository.findByIds(emptyList())

        // then
        assertTrue(actualKeywords.isEmpty())
    }

    @Test
    fun `findByName 성공 테스트`() = runTest {
        // given
        val userId = insertUserAndReturnId()
        val savedKeyword = repository.create(
            TestKeywordName,
            TEST_EMBEDDED_KEYWORD,
            null,
            null,
            userId,
        )

        // when
        val keyword = repository.findByName(savedKeyword.name)

        // then
        assertNotNull(keyword)
        assertEquals(keyword.name, savedKeyword.name)
        assertEquals(keyword.createdBy, savedKeyword.createdBy)
    }

    @Test
    fun `findByName 실패 테스트 - 존재하지 않는 키워드 명 조회`() = runTest {
        // given
        val invalidKeywordName = KeywordName("asd")

        // when
        val keyword = repository.findByName(invalidKeywordName)

        // then
        assertNull(keyword)
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

    companion object {
        private val TestKeywordName = KeywordName("keyword")
        private const val TEST_EMBEDDED_KEYWORD = "[1,0,1]"
        private val TestCategory = KeywordCategory.FOOD // 적절한 카테고리로 교체
        private const val TEST_CATEGORY_SIMILARITY = 0.8f
    }
}
