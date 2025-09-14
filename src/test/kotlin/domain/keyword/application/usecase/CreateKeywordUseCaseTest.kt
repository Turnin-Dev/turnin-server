package com.peekr.domain.keyword.application.usecase

import com.peekr.domain.core.model.KeywordId
import com.peekr.domain.core.model.UserId
import com.peekr.domain.keyword.application.dto.toDto
import com.peekr.domain.keyword.domain.model.Keyword
import com.peekr.domain.keyword.domain.service.KeywordService
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before

class CreateKeywordUseCaseTest {
    private val keywordService = mockk<KeywordService>()
    private lateinit var usecase: CreateKeywordUseCase

    @Before
    fun setUp() {
        usecase = CreateKeywordUseCase(keywordService)
    }

    @Test
    fun `성공적으로 키워드를 생성한다`() = runTest {
        // given
        coEvery { keywordService.create(any(), any()) } returns TestKeyword

        // when
        val keyword = usecase(TEST_KEYWORD, TestUserId)

        // then
        assertEquals(TestKeyword.toDto(), keyword)
    }

    companion object {
        private const val TEST_KEYWORD = "keyword"
        private val TestUserId = UserId(1L)
        private val TestKeyword = Keyword(
            id = KeywordId(1L),
            keyword = TEST_KEYWORD,
            createdBy = TestUserId,
            createdAt = 1000,
            updatedAt = 1000,
        )
    }
}
