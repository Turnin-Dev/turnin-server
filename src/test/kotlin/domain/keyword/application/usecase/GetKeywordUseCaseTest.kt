package com.peekr.domain.keyword.application.usecase

import com.peekr.domain.core.model.KeywordId
import com.peekr.domain.core.model.UserId
import com.peekr.domain.keyword.application.dto.toDto
import com.peekr.domain.keyword.domain.model.Keyword
import com.peekr.domain.keyword.domain.service.KeywordService
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetKeywordUseCaseTest {
    private val keywordService = mockk<KeywordService>()
    private lateinit var usecase: GetKeywordUseCase

    @Before
    fun setUp() {
        usecase = GetKeywordUseCase(keywordService)
    }

    @Test
    fun `키워드를 성공적으로 가져온다`() = runTest {
        // given
        coEvery { keywordService.getKeyword(TestKeyword.id) } returns TestKeyword

        // when
        val keyword = usecase(TestKeyword.id)

        // then
        assertEquals(TestKeyword.toDto(), keyword)
    }

    @Test
    fun `키워드가 존재하지 않으면 null을 반환한다`() = runTest {
        // given
        coEvery { keywordService.getKeyword(TestKeyword.id) } returns null

        // when
        val keyword = usecase(TestKeyword.id)

        // then
        assertNull(keyword)
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
