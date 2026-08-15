package com.turnin.domain.keyword.application.usecase

import com.turnin.common.model.KeywordName
import com.turnin.common.model.KeywordNameValidationException
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.domain.keyword.application.dto.toDto
import com.turnin.domain.keyword.domain.model.Keyword
import com.turnin.domain.keyword.domain.provider.EmbeddingServiceProvider
import com.turnin.domain.keyword.domain.repository.KeywordRepository
import com.turnin.domain.keyword.exception.KeywordException
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.verify
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
    fun `키워드명을 전처리한 뒤 임베딩을 요청한다`() = runTest {
        // given
        val rawKeywordName = "라멘2!!"
        val expectedPreprocessed = "라멘" // 특수문자 제거 + 뒤 숫자 제거
        coEvery {
            embeddingServiceProvider.embed(expectedPreprocessed)
        } returns TEST_EMBEDDED_KEYWORD
        coEvery {
            keywordRepository.create(
                KeywordName(rawKeywordName),
                TEST_EMBEDDED_KEYWORD,
                TestUserId,
            )
        } returns TestKeyword

        // when
        usecase(rawKeywordName, TestUserId)

        // then
        // 임베딩엔 전처리된 문자열이, 저장엔 원본 문자열이 각각 들어가는지 검증
        coVerify(exactly = 1) { embeddingServiceProvider.embed(expectedPreprocessed) }
        coVerify(exactly = 1) {
            keywordRepository.create(
                KeywordName(rawKeywordName),
                TEST_EMBEDDED_KEYWORD,
                TestUserId,
            )
        }
    }

    @Test
    fun `공백과 특수문자가 포함된 키워드도 정상적으로 전처리된다`() = runTest {
        // given
        val rawKeywordName = "네일   바꿈!!"
        val expectedPreprocessed = "네일 바꿈" // 연속 공백 정규화 + 특수문자 제거
        coEvery {
            embeddingServiceProvider.embed(expectedPreprocessed)
        } returns TEST_EMBEDDED_KEYWORD
        coEvery {
            keywordRepository.create(
                KeywordName(rawKeywordName),
                TEST_EMBEDDED_KEYWORD,
                TestUserId,
            )
        } returns TestKeyword

        // when
        usecase(rawKeywordName, TestUserId)

        // then
        coVerify(exactly = 1) { embeddingServiceProvider.embed(expectedPreprocessed) }
    }

    @Test
    fun `전처리 결과가 빈 문자열이면 원본을 그대로 임베딩한다`() = runTest {
        // given
        val rawKeywordName = "123" // 전부 숫자 -> trimEnd 후 빈 문자열 -> 원본 반환
        coEvery {
            embeddingServiceProvider.embed(rawKeywordName)
        } returns TEST_EMBEDDED_KEYWORD
        coEvery {
            keywordRepository.create(
                KeywordName(rawKeywordName),
                TEST_EMBEDDED_KEYWORD,
                TestUserId,
            )
        } returns TestKeyword

        // when
        usecase(rawKeywordName, TestUserId)

        // then
        coVerify(exactly = 1) { embeddingServiceProvider.embed(rawKeywordName) }
    }

    @Test
    fun `키워드 명 유효성 검사 실패 시 에러가 발생한다`() = runTest {
        // given
        val invalidKeywordName = "a".repeat(KeywordName.MAX_LENGTH + 1)

        // when, then
        assertThrows<KeywordNameValidationException> {
            usecase(invalidKeywordName, TestUserId)
        }

        // 검증 실패 시 임베딩/저장 로직이 호출되지 않아야 함
        coVerify(exactly = 0) { embeddingServiceProvider.embed(any()) }
        verify { keywordRepository wasNot Called }
    }

    @Test
    fun `임베딩 실패 시 EmbeddingFailed 예외가 발생한다`() = runTest {
        // given
        coEvery { embeddingServiceProvider.embed(any()) } throws KeywordException.EmbeddingFailed(null)

        // when, then
        assertThrows<KeywordException.EmbeddingFailed> {
            usecase(TestKeywordName.value, TestUserId)
        }

        // 임베딩 실패 시 저장 로직이 호출되지 않아야 함
        coVerify(exactly = 0) { keywordRepository.create(TestKeywordName, any(), TestUserId) }
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
