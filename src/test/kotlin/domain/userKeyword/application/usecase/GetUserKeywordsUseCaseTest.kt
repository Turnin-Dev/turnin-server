package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.KeywordId
import com.peekr.common.model.UserId
import com.peekr.common.model.UserKeywordId
import com.peekr.domain.userKeyword.application.dto.ExternalKeyword
import com.peekr.domain.userKeyword.application.provider.KeywordProvider
import com.peekr.domain.userKeyword.domain.model.UserKeyword
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import com.peekr.domain.userKeyword.exception.UserKeywordException
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before

class GetUserKeywordsUseCaseTest {
    private val userKeywordRepository = mockk<UserKeywordRepository>()
    private val keywordProvider = mockk<KeywordProvider>()
    private lateinit var usecase: GetUserKeywordsUseCase

    @Before
    fun setUp() {
        usecase = GetUserKeywordsUseCase(userKeywordRepository, keywordProvider)
    }

    @Test
    fun `사용자 키워드 목록 조회 성공 테스트`() = runTest {
        // given
        val itemCount = 2
        coEvery {
            userKeywordRepository.findByUserId(TestUserId)
        } returns List(itemCount) { TestUserKeyword }
        coEvery {
            keywordProvider.getKeywordById(TestUserKeyword.keywordId)
        } returns TestExternalKeyword

        // when
        val userKeywords = usecase(TestUserId)

        // then
        assertTrue(userKeywords.size == itemCount)
    }

    @Test
    fun `사용자 키워드 목록 조회 중 존재하지 않은 키워드가 있는 경우 예외가 발생한다`() = runTest {
        // given
        val itemCount = 2
        coEvery {
            userKeywordRepository.findByUserId(TestUserId)
        } returns List(itemCount) { TestUserKeyword }
        coEvery {
            keywordProvider.getKeywordById(TestUserKeyword.keywordId)
        } returns null

        // when
        val exception = runCatching {
            usecase(TestUserId)
        }.exceptionOrNull()

        // then
        assertTrue(exception is UserKeywordException.NotExistsKeyword)
    }

    companion object {
        private val TestUserId = UserId(1)
        private val TestKeywordId = KeywordId(1)
        private val TestUserKeywordId = UserKeywordId(1)
        private const val TEST_KEYWORD = "TestKeyword"
        private val TestUserKeyword = UserKeyword(
            id = TestUserKeywordId,
            userId = TestUserId,
            keywordId = TestKeywordId,
            offsetX = 0.0f,
            offsetY = 0.0f,
            description = "",
            createdAt = 1000,
            updatedAt = 1000,
        )
        private val TestExternalKeyword = ExternalKeyword(
            id = TestKeywordId,
            keyword = TEST_KEYWORD,
            createdBy = TestUserId,
            createdAt = 1000,
            updatedAt = 1000,
        )
    }
}
