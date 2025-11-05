package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.KeywordId
import com.peekr.common.model.UserId
import com.peekr.common.model.UserKeywordId
import com.peekr.domain.userKeyword.application.dto.CreateUserKeywordDto
import com.peekr.domain.userKeyword.application.dto.toDto
import com.peekr.domain.userKeyword.domain.model.Description
import com.peekr.domain.userKeyword.domain.model.Offset
import com.peekr.domain.userKeyword.domain.model.UserKeyword
import com.peekr.domain.userKeyword.domain.provider.ExternalKeyword
import com.peekr.domain.userKeyword.domain.provider.KeywordProvider
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import com.peekr.util.TestDatabaseFactory
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test

class CreateUserKeywordUseCaseTest {
    private val userKeywordRepository = mockk<UserKeywordRepository>()
    private val keywordProviderImpl = mockk<KeywordProvider>()
    private lateinit var usecase: CreateUserKeywordUseCase

    @Before
    fun setUp() {
        TestDatabaseFactory.init()
        usecase = CreateUserKeywordUseCase(userKeywordRepository, keywordProviderImpl)
    }

    @After
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `이미 키워드가 존재하는 경우 해당 키워드 ID로 사용자 키워드를 저장한다`() = runTest {
        // given
        coEvery {
            userKeywordRepository.create(
                keywordId = TestUserKeyword.keywordId,
                userId = TestUserKeyword.userId,
                offset = TestUserKeyword.offset,
                description = TestDescription,
            )
        } returns TestUserKeyword
        coEvery { keywordProviderImpl.findByName(any()) } returns TestExternalKeyword

        // when
        val userKeyword = usecase(TestCreateUserKeywordDto)

        // then
        assertEquals(userKeyword, TestUserKeyword.toDto(TEST_KEYWORD_NAME))
    }

    @Test
    fun `키워드가 존재하지 않는 경우 저장하고 저장된 키워드 ID로 사용자 키워드를 저장한다`() = runTest {
        // given
        coEvery { keywordProviderImpl.create(TEST_KEYWORD_NAME, TestUserId) } returns TestExternalKeyword
        coEvery {
            userKeywordRepository.create(
                keywordId = TestUserKeyword.keywordId,
                userId = TestUserKeyword.userId,
                offset = TestUserKeyword.offset,
                description = TestDescription,
            )
        } returns TestUserKeyword
        coEvery { keywordProviderImpl.findByName(TEST_KEYWORD_NAME) } returns null

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
        private val TestOffset = Offset(0.0f, 0.0f)
        private val TestDescription = Description("test")
        private val TestUserKeyword = UserKeyword(
            id = TestUserKeywordId,
            userId = TestUserId,
            keywordId = TestKeywordId,
            offset = TestOffset,
            createdAt = 1000,
            updatedAt = 1000,
        )
        private val TestCreateUserKeywordDto = CreateUserKeywordDto(
            userId = TestUserKeyword.userId,
            keywordName = TEST_KEYWORD_NAME,
            offset = TestOffset.toDto(),
            description = TestDescription.toDto(),
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
