package com.peekr.domain.keyword.infrastructure.service.impl

import com.peekr.common.model.KeywordId
import com.peekr.common.model.UserId
import com.peekr.domain.keyword.domain.model.Keyword
import com.peekr.domain.keyword.domain.repository.KeywordRepository
import com.peekr.domain.keyword.domain.service.KeywordService
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class KeywordServiceImplTest {
    private val keywordRepository = mockk<KeywordRepository>()
    private lateinit var service: KeywordService

    @Before
    fun setUp() {
        service = KeywordServiceImpl(keywordRepository)
    }

    @Test
    fun `키워드를 성공적으로 가져온다`() = runTest {
        // given
        coEvery { keywordRepository.findById(TestKeyword.id) } returns TestKeyword

        // when
        val keyword = service.getKeyword(TestKeyword.id)

        // then
        assertEquals(keyword, TestKeyword)
    }

    @Test
    fun `키워드가 존재하지 않으면 null을 반환한다`() = runTest {
        // given
        coEvery { keywordRepository.findById(any()) } returns null

        // when
        val keyword = service.getKeyword(TestKeyword.id)

        // then
        assertNull(keyword)
    }

    companion object {
        private val TestKeyword = Keyword(
            id = KeywordId(1L),
            keyword = "keyword",
            createdBy = UserId(1L),
            createdAt = 1000,
            updatedAt = 1000,
        )
    }
}
