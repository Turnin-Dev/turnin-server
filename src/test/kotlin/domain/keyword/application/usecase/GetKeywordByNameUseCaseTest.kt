package com.peekr.domain.keyword.application.usecase

import com.peekr.common.model.KeywordId
import com.peekr.common.model.UserId
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

class GetKeywordByNameUseCaseTest {
    private val keywordService: KeywordService = mockk()
    private lateinit var usecase: GetKeywordByNameUseCase

    @Before
    fun setUp() {
        usecase = GetKeywordByNameUseCase(keywordService)
    }

    @Test
    fun `키워드를 성공적으로 가져온다`() = runTest {
        // given
        coEvery { keywordService.getKeywordByName(TEST_KEYWORD) } returns TestKeyword

        // when
        val keyword = usecase(TEST_KEYWORD)

        // then
        assertEquals(TestKeyword.toDto(), keyword)
    }

    @Test
    fun `키워드가 존재하지 않으면 null을 반환한다`() = runTest {
        // given
        coEvery { keywordService.getKeywordByName(TEST_KEYWORD) } returns null

        // when
        val keyword = usecase(TEST_KEYWORD)

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
