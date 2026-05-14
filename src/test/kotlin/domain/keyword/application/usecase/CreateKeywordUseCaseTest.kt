package com.turnin.domain.keyword.application.usecase

import com.turnin.common.model.KeywordName
import com.turnin.common.model.KeywordNameValidationException
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.domain.keyword.application.dto.toDto
import com.turnin.domain.keyword.domain.model.Keyword
import com.turnin.domain.keyword.domain.provider.EmbeddingServiceProvider
import com.turnin.domain.keyword.domain.repository.KeywordRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.jupiter.api.assertThrows

class CreateKeywordUseCaseTest {
    private val keywordRepository = mockk<KeywordRepository>()
    private val embeddingServiceProvider = mockk<EmbeddingServiceProvider>()
    private lateinit var usecase: CreateKeywordUseCase

    @Before
    fun setUp() {
        usecase = CreateKeywordUseCase(keywordRepository, embeddingServiceProvider)
    }

    @Test
    fun `성공적으로 키워드를 생성한다`() = runTest {
        // given
        coEvery {
            keywordRepository.create(
                TestKeywordName,
                TEST_EMBEDDED_KEYWORD,
                TestUserId,
            )
        } returns TestKeyword
        coEvery { embeddingServiceProvider.embed(any()) } returns TEST_EMBEDDED_KEYWORD

        // when
        val keyword = usecase(TestKeywordName.value, TestUserId)

        // then
        assertEquals(TestKeyword.toDto(), keyword)
    }

    @Test
    fun `키워드 명 유효성 검사 실패 시 에러가 발생한다`() = runTest {
        // given
        val invalidKeywordName = "a".repeat(KeywordName.MAX_LENGTH + 1)

        // when, then
        assertThrows<KeywordNameValidationException> {
            usecase(invalidKeywordName, TestUserId)
        }
    }

    companion object {
        private val TestKeywordName = KeywordName("keyword")
        private val TestUserId = UserId(1L)
        private const val TEST_EMBEDDED_KEYWORD = "[0,1,0]"
        private val TestKeyword = Keyword(
            id = KeywordId(1L),
            name = TestKeywordName,
            embedding = TEST_EMBEDDED_KEYWORD,
            createdBy = TestUserId,
            createdAt = 1000,
            updatedAt = 1000,
        )
    }
}
