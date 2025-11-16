package com.peekr.domain.keyword.application.usecase

import com.peekr.common.model.KeywordId
import com.peekr.common.model.KeywordName
import com.peekr.common.model.UserId
import com.peekr.domain.keyword.application.dto.toDto
import com.peekr.domain.keyword.domain.model.Keyword
import com.peekr.domain.keyword.domain.repository.KeywordRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before

class CreateKeywordUseCaseTest {
    private val keywordRepository = mockk<KeywordRepository>()
    private lateinit var usecase: CreateKeywordUseCase

    @Before
    fun setUp() {
        usecase = CreateKeywordUseCase(keywordRepository)
    }

    @Test
    fun `성공적으로 키워드를 생성한다`() = runTest {
        // given
        coEvery { keywordRepository.create(TestKeywordName, TestUserId) } returns TestKeyword

        // when
        val keyword = usecase(TestKeywordName.value, TestUserId)

        // then
        assertEquals(TestKeyword.toDto(), keyword)
    }

    companion object {
        private val TestKeywordName = KeywordName("keyword")
        private val TestUserId = UserId(1L)
        private val TestKeyword = Keyword(
            id = KeywordId(1L),
            name = TestKeywordName,
            createdBy = TestUserId,
            createdAt = 1000,
            updatedAt = 1000,
        )
    }
}
