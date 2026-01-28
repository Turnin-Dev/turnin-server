package com.peekr.domain.feed.application.usecase

import com.peekr.common.model.KeywordName
import com.peekr.common.model.UserName
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.feed.application.dto.FeedCursor
import com.peekr.domain.feed.application.dto.toDto
import com.peekr.domain.feed.domain.model.Feed
import com.peekr.domain.feed.domain.repository.FeedRepository
import com.peekr.domain.userKeyword.domain.model.Description
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest
import org.junit.Test

class GetFeedsUseCaseTest {
    private val feedRepository: FeedRepository = mockk()
    private val usecase = GetFeedsUseCase(feedRepository)

    @Test
    fun `피드를 커서 페이지네이션으로 두 페이지까지 조회한다`() = runTest {
        // given: 페이지 사이즈, 페이지 개수만큼 피드를 생성한다.
        val pageSize = 2
        // 실제 리포지토리는 pageSize + 1 개의 데이터를 반환한다.
        val expectedFirstPage = createFeed(pageSize + 1)
        val expectedCursor = FeedCursor(
            score = expectedFirstPage.take(pageSize).last().score,
            createdAt = expectedFirstPage.take(pageSize).last().createdAt,
            userKeywordId = expectedFirstPage
                .take(pageSize)
                .last()
                .userKeywordId.value,
        )
        // 두 번째 페이지는 데이터가 부족하게 설정
        val expectedSecondPage = createFeed(pageSize - 1)
        coEvery {
            feedRepository.getFeeds(
                TestUserId,
                null,
                null,
                null,
                pageSize,
            )
        } returns expectedFirstPage
        coEvery {
            feedRepository.getFeeds(
                TestUserId,
                expectedCursor.score,
                expectedCursor.createdAt,
                expectedCursor.userKeywordId?.let { UserKeywordId(it) },
                pageSize,
            )
        } returns expectedSecondPage

        // when: 첫 페이지 호출 (초기 호출이므로 커서 값은 null로 호출)
        val firstPage = usecase(TestUserId.value, null, pageSize)
        // then: 첫 페이지의 데이터 및 커서 검증
        assertEquals(
            expectedFirstPage.take(pageSize).map { it.toDto() },
            firstPage.items,
        )
        assertNotNull(firstPage.nextCursor)

        // when: 두 번째 페이지 호출 (첫 페이지에서 반환된 커서 사용)
        val secondPage = usecase(TestUserId.value, firstPage.nextCursor, pageSize)
        // then: 두 번째 페이지의 데이터 및 커서 검증 (마지막 페이지이므로 반환된 커서 값은 null)
        assertEquals(expectedSecondPage.map { it.toDto() }, secondPage.items)
        assertNull(secondPage.nextCursor)
    }

    private fun createFeed(count: Int) =
        List(count) {
            val id = (it + 1).toLong()
            Feed(
                userKeywordId = UserKeywordId(id),
                userId = UserId(id),
                userName = UserName("username$id"),
                profileImageUrl = "profileImage$id",
                keywordId = KeywordId(id),
                keyword = KeywordName("keyword$id"),
                description = Description("description$id"),
                createdAt = 1000L,
                score = 50.0,
                similarity = 0.8,
            )
        }

    companion object {
        private val TestUserId = UserId(1L)
    }
}
