package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.KeywordId
import com.peekr.common.model.UserId
import com.peekr.common.model.UserKeywordId
import com.peekr.domain.userKeyword.application.dto.CreateUserKeywordDto
import com.peekr.domain.userKeyword.application.dto.ExternalKeyword
import com.peekr.domain.userKeyword.application.dto.toDto
import com.peekr.domain.userKeyword.application.provider.KeywordProvider
import com.peekr.domain.userKeyword.domain.model.UserKeyword
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class CreateUserKeywordUseCaseTest {
    private val userKeywordRepository = mockk<UserKeywordRepository>()
    private val keywordProvider = mockk<KeywordProvider>()
    private lateinit var usecase: CreateUserKeywordUseCase

    @Before
    fun setUp() {
        usecase = CreateUserKeywordUseCase(userKeywordRepository, keywordProvider)
    }

    @Test
    fun `이미 키워드가 존재하는 경우 해당 키워드 ID로 사용자 키워드를 저장한다`() = runTest {
        // given
        coEvery {
            userKeywordRepository.create(
                keywordId = TestUserKeyword.keywordId,
                userId = TestUserKeyword.userId,
                offsetX = TestUserKeyword.offsetX,
                offsetY = TestUserKeyword.offsetY,
                description = TestUserKeyword.description,
            )
        } returns TestUserKeyword
        coEvery { keywordProvider.findByName(any()) } returns TestExternalKeyword

        // when
        val userKeyword = usecase(TestCreateUserKeywordDto)

        // then
        assertEquals(userKeyword, TestUserKeyword.toDto(TEST_KEYWORD_NAME))
    }

    @Test
    fun `키워드가 존재하지 않는 경우 저장하고 저장된 키워드 ID로 사용자 키워드를 저장한다`() = runTest {
        // given
        coEvery { keywordProvider.create(TEST_KEYWORD_NAME, TestUserId) } returns TestExternalKeyword
        coEvery {
            userKeywordRepository.create(
                keywordId = TestUserKeyword.keywordId,
                userId = TestUserKeyword.userId,
                offsetX = TestUserKeyword.offsetX,
                offsetY = TestUserKeyword.offsetY,
                description = TestUserKeyword.description,
            )
        } returns TestUserKeyword
        coEvery { keywordProvider.findByName(TEST_KEYWORD_NAME) } returns null

        // when
        val userKeyword = usecase(TestCreateUserKeywordDto)

        // then
        assertEquals(userKeyword, TestUserKeyword.toDto(TEST_KEYWORD_NAME))
    }

    companion object {
        private val TestUserId = UserId(1)
        private val TestKeywordId = KeywordId(1)
        private val TestUserKeywordId = UserKeywordId(1)
        private const val TEST_KEYWORD_NAME = "sample"
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
        private val TestCreateUserKeywordDto = CreateUserKeywordDto(
            userId = TestUserKeyword.userId,
            keywordName = TEST_KEYWORD_NAME,
            offsetX = TestUserKeyword.offsetX,
            offsetY = TestUserKeyword.offsetY,
            description = TestUserKeyword.description,
        )
        private val TestExternalKeyword = ExternalKeyword(
            id = TestKeywordId,
            keyword = TEST_KEYWORD_NAME,
            createdBy = TestUserId,
            createdAt = 1000,
            updatedAt = 1000,
        )
    }
}
