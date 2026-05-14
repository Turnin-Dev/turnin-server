package com.turnin.domain.userKeyword.application.usecase

import com.turnin.common.model.KeywordName
import com.turnin.common.model.UserName
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.userKeyword.application.dto.toDto
import com.turnin.domain.userKeyword.domain.model.Description
import com.turnin.domain.userKeyword.domain.model.UserInfo
import com.turnin.domain.userKeyword.domain.model.UserKeywordDetail
import com.turnin.domain.userKeyword.domain.repository.UserKeywordRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class GetDetailUseCaseTest {
    private val userKeywordRepository = mockk<UserKeywordRepository>()
    private val usecase = GetDetailUseCase(userKeywordRepository)

    @Test
    fun `사용자 키워드 상세 정보 조회 시 정상적으로 값을 반환한다`() = runTest {
        // given
        coEvery {
            userKeywordRepository.getDetailById(TestCurrentUserId, TestUserKeywordId)
        } returns TestUserKeywordDetail

        // when
        val result = usecase(TestCurrentUserId.value, TestUserKeywordId.value)

        // then
        assertNotNull(result)
        assertEquals(TestUserKeywordDetail.toDto(), result)
    }

    @Test
    fun `리포지토리에서 값을 찾지 못할 때 NULL을 반환한다`() = runTest {
        // given
        coEvery {
            userKeywordRepository.getDetailById(TestCurrentUserId, TestUserKeywordId)
        } returns null

        // when
        val result = usecase(TestCurrentUserId.value, TestUserKeywordId.value)

        // then
        assertNull(result)
    }

    companion object {
        private val TestCurrentUserId = UserId(1L)
        private val TestUserKeywordId = UserKeywordId(1L)
        private val TestUserKeywordDetail = UserKeywordDetail(
            userKeywordId = TestUserKeywordId,
            keywordId = KeywordId(1L),
            keywordName = KeywordName("keyword"),
            description = Description("description"),
            userInfo = UserInfo(
                userId = UserId(1L),
                userName = UserName("user"),
                profileImageUrl = "https://image.com/image.jpg",
            ),
            createdAt = 1000,
            updatedAt = 1000,
        )
    }
}
