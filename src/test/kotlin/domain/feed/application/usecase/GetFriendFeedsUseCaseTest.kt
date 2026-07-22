package com.turnin.domain.feed.application.usecase

import com.turnin.common.model.KeywordName
import com.turnin.common.model.UserName
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.feed.application.dto.FeedCursor
import com.turnin.domain.feed.application.dto.FeedCursorCodec
import com.turnin.domain.feed.application.dto.toDto
import com.turnin.domain.feed.domain.model.Feed
import com.turnin.domain.feed.domain.model.FeedWindowResult
import com.turnin.domain.feed.domain.repository.FeedRepository
import com.turnin.domain.userKeyword.domain.model.Description
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetFriendFeedsUseCaseTest {
    private val feedRepository = mockk<FeedRepository>()
    private val usecase = GetFriendFeedsUseCase(feedRepository)

    @Test
    fun `cursor가 없으면 새 seed를 발급해 sessionMaxId, windowAnchorId 없이 첫 청크를 요청한다`() = runTest {
        // given
        val seedSlot = slot<String>()
        coEvery {
            feedRepository.getFriendFeeds(
                userId = TestUserId,
                seed = capture(seedSlot),
                sessionMaxId = null,
                windowAnchorId = null,
                lastShuffleKey = null,
                lastUkId = null,
                windowSize = DEFAULT_WINDOW_SIZE,
                limit = TEST_LIMIT,
            )
        } returns TestWindowResult

        // when
        val page = usecase(TestUserId, cursorRaw = null, limit = TEST_LIMIT)

        // then
        assertTrue(seedSlot.captured.isNotBlank())
        assertEquals(TestWindowResult.feeds.map { it.toDto() }, page.items)
        assertNotNull(page.nextCursor)
    }

    @Test
    fun `유효한 cursor가 있으면 디코딩한 값으로 다음 청크를 요청한다`() = runTest {
        // given
        val cursor = FeedCursor(
            seed = "existing-seed",
            sessionMaxId = 500L,
            windowAnchorId = 42L,
            lastShuffleKey = 7,
            lastUkId = 20L,
        )
        val cursorRaw = FeedCursorCodec.encode(cursor)

        coEvery {
            feedRepository.getFriendFeeds(
                userId = TestUserId,
                seed = "existing-seed",
                sessionMaxId = 500L,
                windowAnchorId = UserKeywordId(42L),
                lastShuffleKey = 7,
                lastUkId = 20L,
                windowSize = DEFAULT_WINDOW_SIZE,
                limit = TEST_LIMIT,
            )
        } returns TestWindowResult

        // when
        val page = usecase(TestUserId, cursorRaw = cursorRaw, limit = TEST_LIMIT)

        // then
        coVerify(exactly = 1) {
            feedRepository.getFriendFeeds(
                userId = TestUserId,
                seed = "existing-seed",
                sessionMaxId = 500L,
                windowAnchorId = UserKeywordId(42L),
                lastShuffleKey = 7,
                lastUkId = 20L,
                windowSize = DEFAULT_WINDOW_SIZE,
                limit = TEST_LIMIT,
            )
        }
        assertEquals(TestWindowResult.feeds.map { it.toDto() }, page.items)
    }

    @Test
    fun `cursor 디코딩에 실패하면 손상된 것으로 간주하고 첫 페이지로 취급한다`() = runTest {
        // given
        coEvery {
            feedRepository.getFriendFeeds(
                userId = TestUserId,
                seed = any(),
                sessionMaxId = null,
                windowAnchorId = null,
                lastShuffleKey = null,
                lastUkId = null,
                windowSize = DEFAULT_WINDOW_SIZE,
                limit = TEST_LIMIT,
            )
        } returns TestWindowResult

        // when
        val page = usecase(TestUserId, cursorRaw = "not-a-valid-cursor!!", limit = TEST_LIMIT)

        // then
        coVerify(exactly = 1) {
            feedRepository.getFriendFeeds(
                userId = TestUserId,
                seed = any(),
                sessionMaxId = null,
                windowAnchorId = null,
                lastShuffleKey = null,
                lastUkId = null,
                windowSize = DEFAULT_WINDOW_SIZE,
                limit = TEST_LIMIT,
            )
        }
        assertEquals(TestWindowResult.feeds.map { it.toDto() }, page.items)
    }

    @Test
    fun `생성자에 windowSize를 지정하면 해당 값이 리포지토리에 전달된다`() = runTest {
        // given
        val customUsecase = GetFriendFeedsUseCase(feedRepository, windowSize = CUSTOM_WINDOW_SIZE)
        coEvery {
            feedRepository.getFriendFeeds(
                userId = TestUserId,
                seed = any(),
                sessionMaxId = null,
                windowAnchorId = null,
                lastShuffleKey = null,
                lastUkId = null,
                windowSize = CUSTOM_WINDOW_SIZE,
                limit = TEST_LIMIT,
            )
        } returns TestWindowResult

        // when
        customUsecase(TestUserId, cursorRaw = null, limit = TEST_LIMIT)

        // then
        coVerify(exactly = 1) {
            feedRepository.getFriendFeeds(
                userId = TestUserId,
                seed = any(),
                sessionMaxId = null,
                windowAnchorId = null,
                lastShuffleKey = null,
                lastUkId = null,
                windowSize = CUSTOM_WINDOW_SIZE,
                limit = TEST_LIMIT,
            )
        }
    }

    @Test
    fun `조회된 결과가 없으면 빈 목록과 null 커서를 반환한다`() = runTest {
        // given
        coEvery {
            feedRepository.getFriendFeeds(
                userId = TestUserId,
                seed = any(),
                sessionMaxId = null,
                windowAnchorId = null,
                lastShuffleKey = null,
                lastUkId = null,
                windowSize = DEFAULT_WINDOW_SIZE,
                limit = TEST_LIMIT,
            )
        } returns TestEmptyWindowResult

        // when
        val page = usecase(TestUserId, cursorRaw = null, limit = TEST_LIMIT)

        // then
        assertTrue(page.items.isEmpty())
        assertNull(page.nextCursor)
    }

    companion object {
        private const val DEFAULT_WINDOW_SIZE = 3000
        private const val CUSTOM_WINDOW_SIZE = 100
        private const val TEST_LIMIT = 20

        private val TestUserId = UserId(1L)

        private val TestFeed = Feed(
            userKeywordId = UserKeywordId(10L),
            userId = UserId(2L),
            userName = UserName("friend"),
            profileImageUrl = null,
            keywordId = KeywordId(3L),
            keyword = KeywordName("kotlin"),
            description = Description("설명"),
            createdAt = 1_700_000_000_000L,
        )

        private val TestWindowResult = FeedWindowResult(
            feeds = listOf(TestFeed),
            windowMinUkId = 10L,
            windowFetchedCount = 1,
            sessionMaxId = 999L,
            lastShuffleKey = 5,
            lastUkId = 10L,
        )

        private val TestEmptyWindowResult = FeedWindowResult(
            feeds = emptyList(),
            windowMinUkId = null,
            windowFetchedCount = 0,
            sessionMaxId = null,
            lastShuffleKey = null,
            lastUkId = null,
        )
    }
}
