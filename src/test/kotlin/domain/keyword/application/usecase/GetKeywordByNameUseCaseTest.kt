package com.peekr.domain.keyword.application.usecase

import com.peekr.common.model.KeywordName
import com.peekr.common.model.KeywordNameValidationException
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.domain.keyword.application.dto.toDto
import com.peekr.domain.keyword.domain.model.Keyword
import com.peekr.domain.keyword.domain.repository.KeywordRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.jupiter.api.assertThrows

class GetKeywordByNameUseCaseTest {
    private val keywordRepository: KeywordRepository = mockk()
    private lateinit var usecase: GetKeywordByNameUseCase

    @Before
    fun setUp() {
        usecase = GetKeywordByNameUseCase(keywordRepository)
    }

    @Test
    fun `키워드를 성공적으로 가져온다`() = runTest {
        // given
        coEvery { keywordRepository.findByName(TestKeywordName) } returns TestKeyword

        // when
        val keyword = usecase(TestKeywordName.value)

        // then
        assertEquals(TestKeyword.toDto(), keyword)
    }

    @Test
    fun `키워드가 존재하지 않으면 null을 반환한다`() = runTest {
        // given
        coEvery { keywordRepository.findByName(TestKeywordName) } returns null

        // when
        val keyword = usecase(TestKeywordName.value)

        // then
        assertNull(keyword)
    }

    @Test
    fun `키워드 명 유효성 검사 실패 시 에러가 발생한다`() = runTest {
        // given
        val invalidKeywordName = "a".repeat(KeywordName.MAX_LENGTH + 1)

        // when, then
        assertThrows<KeywordNameValidationException> {
            usecase(invalidKeywordName)
        }
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
