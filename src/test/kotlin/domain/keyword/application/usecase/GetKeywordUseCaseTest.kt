package com.turnin.domain.keyword.application.usecase

import com.turnin.common.model.KeywordName
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.domain.keyword.application.dto.toDto
import com.turnin.domain.keyword.domain.model.Keyword
import com.turnin.domain.keyword.domain.repository.KeywordRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetKeywordUseCaseTest {
    private val keywordRepository = mockk<KeywordRepository>()
    private lateinit var usecase: GetKeywordUseCase

    @Before
    fun setUp() {
        usecase = GetKeywordUseCase(keywordRepository)
    }

    @Test
    fun `키워드를 성공적으로 가져온다`() = runTest {
        // given
        coEvery { keywordRepository.findById(TestKeyword.id) } returns TestKeyword

        // when
        val keyword = usecase(TestKeyword.id)

        // then
        assertEquals(TestKeyword.toDto(), keyword)
    }

    @Test
    fun `키워드가 존재하지 않으면 null을 반환한다`() = runTest {
        // given
        coEvery { keywordRepository.findById(TestKeyword.id) } returns null

        // when
        val keyword = usecase(TestKeyword.id)

        // then
        assertNull(keyword)
    }

    companion object {
        private val TestKeywordName = KeywordName("keyword")
        private val TestUserId = UserId(1L)
        private val TestKeyword = Keyword(
            id = KeywordId(1L),
            name = TestKeywordName,
            embedding = "[0,1,0]",
            createdBy = TestUserId,
            createdAt = 1000,
            updatedAt = 1000,
        )
    }
}
