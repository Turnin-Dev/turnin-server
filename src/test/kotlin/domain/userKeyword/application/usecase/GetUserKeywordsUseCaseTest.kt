package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.KeywordId
import com.peekr.common.model.UserId
import com.peekr.common.model.UserKeywordId
import com.peekr.domain.userKeyword.domain.model.UserKeyword
import com.peekr.domain.userKeyword.domain.service.UserKeywordService
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class GetUserKeywordsUseCaseTest {
    private val userKeywordService = mockk<UserKeywordService>()
    private lateinit var usecase: GetUserKeywordsUseCase

    @Before
    fun setUp() {
        usecase = GetUserKeywordsUseCase(userKeywordService)
    }

    @Test
    fun `사용자 키워드 목록 조회 성공 테스트`() = runTest {
        // given
        val itemCount = 2
        coEvery {
            userKeywordService.getKeywords(TestUserId)
        } returns List(itemCount) { TestUserKeyword }

        // when
        val userKeywords = usecase(TestUserId)

        // then
        assertTrue(userKeywords.size == itemCount)
    }

    companion object {
        private val TestUserId = UserId(1)
        private val TestKeywordId = KeywordId(1)
        private val TestUserKeywordId = UserKeywordId(1)
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
    }
}
