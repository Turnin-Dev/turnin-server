package com.turnin.domain.keyword.application.usecase

import com.turnin.common.ml.keywordCategory.CategoryClassificationResult
import com.turnin.common.ml.keywordCategory.KeywordCategory
import com.turnin.common.ml.keywordCategory.KeywordCategoryClassifier
import com.turnin.common.model.KeywordName
import com.turnin.common.model.KeywordNameValidationException
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.domain.keyword.application.dto.toDto
import com.turnin.domain.keyword.domain.model.Keyword
import com.turnin.domain.keyword.domain.repository.KeywordRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.jupiter.api.assertThrows

class CreateKeywordUseCaseTest {
    private val keywordRepository = mockk<KeywordRepository>()
    private val keywordCategoryClassifier = mockk<KeywordCategoryClassifier>()
    private lateinit var usecase: CreateKeywordUseCase

    @Before
    fun setUp() {
        usecase = CreateKeywordUseCase(
            keywordRepository,
            keywordCategoryClassifier,
        )
    }

    @Test
    fun `성공적으로 키워드를 생성한다 - 카테고리 분류 성공`() = runTest {
        // given
        val classificationResult = CategoryClassificationResult(
            category = KeywordCategory.FOOD,
            similarity = 0.8f,
            preprocessedKeywordVector = TEST_EMBEDDED_KEYWORD,
        )
        every { keywordCategoryClassifier.classify(any()) } returns classificationResult
        coEvery {
            keywordRepository.create(
                TestKeywordName,
                TEST_EMBEDDED_KEYWORD,
                classificationResult.category,
                classificationResult.similarity,
                TestUserId,
            )
        } returns TestKeyword

        // when
        val keyword = usecase(TestKeywordName.value, TestUserId)

        // then
        assertEquals(TestKeyword.toDto(), keyword)
        coVerify(exactly = 1) { keywordCategoryClassifier.classify(any()) }
    }

    @Test
    fun `성공적으로 키워드를 생성한다 - 카테고리 미분류`() = runTest {
        // given
        // 미분류
        every {
            keywordCategoryClassifier.classify(any())
        } returns CategoryClassificationResult(null, null, TEST_EMBEDDED_KEYWORD)
        coEvery {
            keywordRepository.create(
                TestKeywordName,
                TEST_EMBEDDED_KEYWORD,
                null,
                null,
                TestUserId,
            )
        } returns TestKeywordUnclassified

        // when
        val keyword = usecase(TestKeywordName.value, TestUserId)

        // then
        assertEquals(TestKeywordUnclassified.toDto(), keyword)
        coVerify(exactly = 1) { keywordCategoryClassifier.classify(any()) }
    }

    @Test
    fun `키워드 명 유효성 검사 실패 시 에러가 발생한다`() = runTest {
        // given
        val invalidKeywordName = "a".repeat(KeywordName.MAX_LENGTH + 1)

        // when, then
        assertThrows<KeywordNameValidationException> {
            usecase(invalidKeywordName, TestUserId)
        }
        coVerify(exactly = 0) { keywordCategoryClassifier.classify(any()) }
    }

    companion object {
        private val TestKeywordName = KeywordName("keyword")
        private val TestUserId = UserId(1L)
        private const val TEST_EMBEDDED_KEYWORD = "[0,1,0]"
        private val TestKeyword = Keyword(
            id = KeywordId(1L),
            name = TestKeywordName,
            embedding = TEST_EMBEDDED_KEYWORD,
            category = KeywordCategory.FOOD,
            categorySimilarity = 0.8,
            createdBy = TestUserId,
            createdAt = 1000,
            updatedAt = 1000,
        )
        private val TestKeywordUnclassified = Keyword(
            id = KeywordId(2L),
            name = TestKeywordName,
            embedding = TEST_EMBEDDED_KEYWORD,
            category = null,
            categorySimilarity = null,
            createdBy = TestUserId,
            createdAt = 1000,
            updatedAt = 1000,
        )
    }
}
