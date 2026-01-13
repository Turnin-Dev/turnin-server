package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.KeywordName
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.userKeyword.application.dto.toDto
import com.peekr.domain.userKeyword.domain.model.Description
import com.peekr.domain.userKeyword.domain.model.UserKeywordDetail
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.coroutines.test.runTest

class GetDetailsUseCaseTest {
    private val userKeywordRepository = mockk<UserKeywordRepository>()
    private val usecase = GetDetailsUseCase(userKeywordRepository)

    @Test
    fun `사용자 ID로 사용자 키워드 상세 정보 리스트 조회 성공 테스트`() = runTest {
        // given
        val expectedCount = 3
        val expectedList = List(expectedCount) { TestUserKeywordDetail }
        coEvery {
            userKeywordRepository.getDetailsByUserId(TestUserId)
        } returns expectedList

        // when
        val result = usecase(TestUserId.value)

        // then
        assertTrue(result.isNotEmpty())
        assertEquals(expectedCount, result.size)
        assertEquals(expectedList.map { it.toDto() }, result)
    }

    @Test
    fun `데이터가 존재하지 않는 경우 빈 리스트를 반환한다`() = runTest {
        // given
        coEvery {
            userKeywordRepository.getDetailsByUserId(TestUserId)
        } returns emptyList()

        // when
        val result = usecase(TestUserId.value)

        // then
        assertTrue(result.isEmpty())
    }

    companion object {
        private val TestUserId = UserId(1L)
        private val TestUserKeywordDetail = UserKeywordDetail(
            userKeywordId = UserKeywordId(1L),
            keywordId = KeywordId(1L),
            keywordName = KeywordName("keyword"),
            description = Description("description"),
            userInfo = null,
            createdAt = 1000,
            updatedAt = 1000,
        )
    }
}
