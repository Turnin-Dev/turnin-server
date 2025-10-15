package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.KeywordId
import com.peekr.common.model.UserId
import com.peekr.common.model.UserKeywordId
import com.peekr.domain.userKeyword.application.dto.CreateUserKeywordDto
import com.peekr.domain.userKeyword.application.dto.toDto
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
    private lateinit var usecase: CreateUserKeywordUseCase

    @Before
    fun setUp() {
        usecase = CreateUserKeywordUseCase(userKeywordRepository)
    }

    @Test
    fun `사용자 키워드 생성 성공 테스트`() = runTest {
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

        // when
        val userKeyword = usecase(TestCreateUserKeywordDto)

        // then
        assertEquals(userKeyword, TestUserKeyword.toDto())
    }

    companion object {
        private val TestUserId = UserId(1)
        private val TestKeywordId = KeywordId(1)
        private val TestUserKeywordId = UserKeywordId(1)
        private val TestUserKeyword = UserKeyword(
            id = TestUserKeywordId,
            userId = TestUserId,
            keywordId = TestKeywordId,
            keywordName = "sample",
            offsetX = 0.0f,
            offsetY = 0.0f,
            description = "",
            createdAt = 1000,
            updatedAt = 1000,
        )
        private val TestCreateUserKeywordDto = CreateUserKeywordDto(
            keywordId = TestUserKeyword.keywordId,
            userId = TestUserKeyword.userId,
            offsetX = TestUserKeyword.offsetX,
            offsetY = TestUserKeyword.offsetY,
            description = TestUserKeyword.description,
        )
    }
}
