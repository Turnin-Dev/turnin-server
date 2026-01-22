package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.KeywordName
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.userKeyword.application.dto.UserKeywordPatchDto
import com.peekr.domain.userKeyword.domain.provider.ExternalKeyword
import com.peekr.domain.userKeyword.domain.provider.KeywordProvider
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest
import org.junit.Before

class UpdateUserKeywordUseCaseTest {
    private val userKeywordRepository = mockk<UserKeywordRepository>()
    private val keywordProvider = mockk<KeywordProvider>()
    private lateinit var usecase: UpdateUserKeywordUseCase

    @Before
    fun setUp() {
        usecase = UpdateUserKeywordUseCase(userKeywordRepository, keywordProvider)
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

        // when
        val result = usecase(TestUserId.value, TestUserKeywordPatchDto)

        // then
        assertTrue(result)
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

        // when
        val result = usecase(TestUserId.value, TestUserKeywordPatchDto)

        // then
        assertTrue(result)
    }

    @Test
    fun `사용자 키워드 수정에 실패한 경우 false를 반환한다`() = runTest {
        // given
        coEvery {
            userKeywordRepository.update(TestUserId, any())
        } returns false
        coEvery {
            keywordProvider.findByName(any())
        } returns TestExternalKeyword

        // when
        val result = usecase(TestUserId.value, TestUserKeywordPatchDto)

        // then
        assertFalse(result)
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
