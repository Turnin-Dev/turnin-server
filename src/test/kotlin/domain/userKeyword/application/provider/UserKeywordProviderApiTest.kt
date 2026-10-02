package com.turnin.domain.userKeyword.application.provider

import com.turnin.common.model.KeywordName
import com.turnin.common.model.UserName
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.userKeyword.domain.model.Description
import com.turnin.domain.userKeyword.domain.model.UserInfo
import com.turnin.domain.userKeyword.domain.model.UserKeywordDetail
import com.turnin.domain.userKeyword.domain.repository.UserKeywordRepository
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class UserKeywordProviderApiTest {
    private val userKeywordRepository = mockk<UserKeywordRepository>()
    private val providerApi = UserKeywordProviderApi(userKeywordRepository)

    @Test
    fun `조회할 수 있는 사용자 키워드라면 작성자 ID를 반환한다`() = runTest {
        // given
        coEvery {
            userKeywordRepository.getDetailById(UserId(1L), UserKeywordId(3L))
        } returns UserKeywordDetail(
            userKeywordId = UserKeywordId(3L),
            keywordId = KeywordId(1L),
            keywordName = KeywordName("keyword"),
            description = Description("description"),
            userInfo = UserInfo(
                userId = UserId(2L),
                userName = UserName("user"),
                profileImageUrl = null,
            ),
            createdAt = 1000,
            updatedAt = 1000,
        )

        // when
        val result = providerApi.findOwnerId(UserId(1L), UserKeywordId(3L))

        // then
        assertEquals(UserId(2L), result)
    }

    @Test
    fun `사용자 키워드를 조회할 수 없으면 null을 반환한다`() = runTest {
        // given
        coEvery {
            userKeywordRepository.getDetailById(UserId(1L), UserKeywordId(3L))
        } returns null

        // when
        val result = providerApi.findOwnerId(UserId(1L), UserKeywordId(3L))

        // then
        assertNull(result)
    }
}
