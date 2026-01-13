package com.peekr.domain.userKeyword.application.usecase

import com.peekr.common.model.KeywordName
import com.peekr.common.model.UserName
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.userKeyword.application.dto.toDto
import com.peekr.domain.userKeyword.domain.model.Description
import com.peekr.domain.userKeyword.domain.model.UserInfo
import com.peekr.domain.userKeyword.domain.model.UserKeywordDetail
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
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
    fun `사용자 정보를 포함하여 사용자 키워드 상세 정보 조회 시 정상적으로 값을 반환한다`() = runTest {
        // given
        coEvery {
            userKeywordRepository.getDetailById(TestUserKeywordId, true)
        } returns TestUserKeywordDetail

        // when
        val result = usecase(TestUserKeywordId.value, true)

        // then
        assertNotNull(result)
        assertEquals(TestUserKeywordDetail.toDto(), result)
    }

    @Test
    fun `사용자 정보를 포함하지 않고 사용자 키워드 상세 정보 조회 시 정상적으로 값을 반환한다`() = runTest {
        // given
        val expectedDetail = TestUserKeywordDetail.copy(userInfo = null)
        coEvery {
            userKeywordRepository.getDetailById(TestUserKeywordId, false)
        } returns expectedDetail

        // when
        val result = usecase(TestUserKeywordId.value, false)

        // then
        assertNotNull(result)
        assertEquals(expectedDetail.toDto(), result)
    }

    @Test
    fun `리포지토리에서 값을 찾지 못할 때 NULL을 반환한다`() = runTest {
        // given
        coEvery {
            userKeywordRepository.getDetailById(TestUserKeywordId, false)
        } returns null

        // when
        val result = usecase(TestUserKeywordId.value, false)

        // then
        assertNull(result)
    }

    companion object {
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
