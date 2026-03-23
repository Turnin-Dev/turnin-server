package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.KeywordName
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.userKeyword.application.dto.toDto
import com.peekr.domain.userKeyword.domain.model.Description
import com.peekr.domain.userKeyword.domain.model.ExternalKeyword
import com.peekr.domain.userKeyword.domain.model.UserKeyword
import com.peekr.domain.userKeyword.domain.provider.KeywordProvider
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import com.peekr.domain.userKeyword.exception.UserKeywordException
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before

class GetUserKeywordsUseCaseTest {
    private val userKeywordRepository = mockk<UserKeywordRepository>()
    private val keywordProviderImpl = mockk<KeywordProvider>()
    private lateinit var usecase: GetUserKeywordsUseCase

    @Before
    fun setUp() {
        usecase = GetUserKeywordsUseCase(userKeywordRepository, keywordProviderImpl)
    }

    @Test
    fun `사용자 키워드 목록 조회 성공 테스트`() = runTest {
        // given
        val itemCount = 2
        val expectedUserKeywords = List(itemCount) { TestUserKeyword.copy(id = UserKeywordId((it + 1).toLong())) }
        coEvery {
            userKeywordRepository.findListByUserId(TestUserId)
        } returns expectedUserKeywords
        coEvery {
            keywordProviderImpl.findByIds(any())
        } returns List(itemCount) { TestExternalKeyword.copy(id = KeywordId((it + 1).toLong())) }

        // when
        val userKeywords = usecase(TestUserId.value)

        // then
        assertTrue(userKeywords.size == itemCount)
        assertEquals(
            expectedUserKeywords.map { it.toDto(TestKeywordName.value) },
            userKeywords,
        )
    }

    @Test
    fun `사용자 키워드 목록 조회 중 존재하지 않은 키워드가 있는 경우 예외가 발생한다`() = runTest {
        // given
        val itemCount = 2
        coEvery {
            userKeywordRepository.findListByUserId(TestUserId)
        } returns List(itemCount) { TestUserKeyword }
        coEvery {
            keywordProviderImpl.findByIds(any())
        } returns emptyList()

        // when
        val exception = runCatching {
            usecase(TestUserId.value)
        }.exceptionOrNull()

        // then
        assertTrue(exception is UserKeywordException.NotExistsKeyword)
    }

    companion object {
        private val TestUserId = UserId(1)
        private val TestKeywordId = KeywordId(1)
        private val TestUserKeywordId = UserKeywordId(1)
        private val TestKeywordName = KeywordName("keyword")
        private val TestUserKeyword = UserKeyword(
            id = TestUserKeywordId,
            userId = TestUserId,
            keywordId = TestKeywordId,
            description = Description(""),
            createdAt = 1000,
            updatedAt = 1000,
        )
        private val TestExternalKeyword = ExternalKeyword(
            id = TestKeywordId,
            name = TestKeywordName,
            createdBy = TestUserId,
            createdAt = 1000,
            updatedAt = 1000,
        )
    }
}
