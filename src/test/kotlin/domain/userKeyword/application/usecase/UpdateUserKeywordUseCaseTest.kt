package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.KeywordName
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.userKeyword.application.dto.UserKeywordPatchDto
import com.peekr.domain.userKeyword.domain.model.ExternalKeyword
import com.peekr.domain.userKeyword.domain.provider.KeywordProvider
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import com.peekr.domain.userKeyword.exception.UserKeywordException
import com.peekr.util.db.TestDatabaseFactory
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class UpdateUserKeywordUseCaseTest {
    private val userKeywordRepository = mockk<UserKeywordRepository>()
    private val keywordProvider = mockk<KeywordProvider>()
    private lateinit var usecase: UpdateUserKeywordUseCase

    @Before
    fun setUp() {
        TestDatabaseFactory.init()

        usecase = UpdateUserKeywordUseCase(userKeywordRepository, keywordProvider)
    }

    @After
    fun tearDown() {
        TestDatabaseFactory.cleanUp()
    }

    @Test
    fun `사용자 키워드 수정 성공 테스트 - 키워드가 기존에 있는 경우`() = runTest {
        // given
        coEvery {
            userKeywordRepository.update(TestUserId, any())
        } returns true
        coEvery {
            keywordProvider.findByName(any())
        } returns TestExternalKeyword

        // when, then
        assertDoesNotThrow {
            usecase(TestUserId.value, TestUserKeywordPatchDto)
        }
    }

    @Test
    fun `사용자 키워드 수정 성공 테스트 - 키워드가 기존에 없는 경우`() = runTest {
        // given
        coEvery {
            userKeywordRepository.update(TestUserId, any())
        } returns true
        coEvery {
            keywordProvider.findByName(any())
        } returns null
        coEvery {
            keywordProvider.create(any(), TestUserId)
        } returns TestExternalKeyword

        // when, then
        assertDoesNotThrow {
            usecase(TestUserId.value, TestUserKeywordPatchDto)
        }
    }

    @Test
    fun `사용자 키워드 수정에 실패한 경우 예외를 반환한다`() = runTest {
        // given
        coEvery {
            userKeywordRepository.update(TestUserId, any())
        } returns false
        coEvery {
            keywordProvider.findByName(any())
        } returns TestExternalKeyword

        // when, then
        assertThrows<UserKeywordException.UpdateFailed> {
            usecase(TestUserId.value, TestUserKeywordPatchDto)
        }
    }

    companion object {
        private val TestUserId = UserId(1)
        private val TestUserKeywordId = UserKeywordId(1)
        private val TestUserKeywordPatchDto = UserKeywordPatchDto(
            userKeywordId = TestUserKeywordId.value,
            keywordName = "newKeywordName",
            description = "newDescription",
        )
        private val TestExternalKeyword = ExternalKeyword(
            id = KeywordId(1L),
            name = KeywordName("keyword"),
            createdBy = TestUserId,
            createdAt = 1000L,
            updatedAt = 1000L,
        )
    }
}
