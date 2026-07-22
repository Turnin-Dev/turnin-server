package com.turnin.domain.feed.application.usecase

import com.turnin.common.model.KeywordName
import com.turnin.common.model.UserName
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.feed.application.dto.FeedCursor
import com.turnin.domain.feed.application.dto.FeedCursorCodec
import com.turnin.domain.feed.application.dto.FeedDto
import com.turnin.domain.feed.domain.model.Feed
import com.turnin.domain.feed.domain.model.FeedWindowResult
import com.turnin.domain.userKeyword.domain.model.Description
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.slot
import io.mockk.unmockkObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FeedWindowResultToCursorPageTest {
    private val baseCursor = FeedCursor(
        seed = "seed-0001",
        sessionMaxId = 100L,
        windowAnchorId = 50L,
        lastShuffleKey = 1234,
        lastUkId = 10L,
    )

    @After
    fun tearDown() {
        unmockkObject(FeedCursorCodec)
    }

    private fun testFeed(id: Long): Feed = Feed(
        userKeywordId = UserKeywordId(id),
        userId = UserId(id),
        userName = UserName("user-$id"),
        profileImageUrl = null,
        keywordId = KeywordId(id),
        keyword = KeywordName("keyword-$id"),
        description = Description("desc-$id"),
        createdAt = 1_700_000_000_000L + id,
    )

    private fun testFeedDto(id: Long): FeedDto = FeedDto(
        userKeywordId = id,
        userId = id,
        userName = "user-$id",
        profileImageUrl = null,
        keywordId = id,
        keyword = "keyword-$id",
        description = "desc-$id",
        createdAt = 1_700_000_000_000L + id,
    )

    @Test
    fun `windowFetchedCount이 0이면 빈 결과와 null 커서를 반환한다`() {
        val result = FeedWindowResult(
            windowFetchedCount = 0,
            sessionMaxId = null,
            feeds = emptyList(),
            windowMinUkId = null,
            lastShuffleKey = null,
            lastUkId = null,
        )

        val page = result.toCursorPage(baseCursor, limit = 20)

        assertTrue(page.items.isEmpty())
        assertNull(page.nextCursor)
    }

    @Test
    fun `feeds 크기가 limit보다 작으면 다음 윈도우로 이동하는 커서를 생성하고 feeds를 toDto로 매핑한다`() {
        mockkObject(FeedCursorCodec)
        val cursorSlot = slot<FeedCursor>()
        every { FeedCursorCodec.encode(capture(cursorSlot)) } returns "encoded-cursor"

        val feeds = listOf(testFeed(1L), testFeed(2L))
        val result = FeedWindowResult(
            windowFetchedCount = 2,
            sessionMaxId = 200L,
            feeds = feeds,
            windowMinUkId = 5L,
            lastShuffleKey = 999,
            lastUkId = 999L,
        )

        // feeds.size(2) < limit(20) -> 다음 윈도우로 넘어가는 분기
        val page = result.toCursorPage(baseCursor, limit = 20)

        assertEquals(listOf(testFeedDto(1L), testFeedDto(2L)), page.items)
        assertEquals("encoded-cursor", page.nextCursor)

        val encodedCursor = cursorSlot.captured
        assertEquals(200L, encodedCursor.sessionMaxId) // 결과의 sessionMaxId로 갱신됨
        assertEquals(5L, encodedCursor.windowAnchorId) // windowMinUkId로 이동
        assertNull(encodedCursor.lastShuffleKey) // 다음 윈도우이므로 초기화
        assertNull(encodedCursor.lastUkId) // 다음 윈도우이므로 초기화
        assertEquals(baseCursor.seed, encodedCursor.seed) // seed는 그대로 유지
    }

    @Test
    fun `feeds 크기가 limit 이상이면 같은 윈도우 내에서 이어지는 커서를 생성한다`() {
        mockkObject(FeedCursorCodec)
        val cursorSlot = slot<FeedCursor>()
        every { FeedCursorCodec.encode(capture(cursorSlot)) } returns "encoded-cursor"

        val feeds = (1..20L).map { testFeed(it) }
        val result = FeedWindowResult(
            windowFetchedCount = 20,
            sessionMaxId = 300L,
            feeds = feeds,
            windowMinUkId = 1L,
            lastShuffleKey = 555,
            lastUkId = 20L,
        )

        // feeds.size(20) >= limit(20) -> 같은 윈도우를 이어가는 분기
        val page = result.toCursorPage(baseCursor, limit = 20)

        assertEquals(20, page.items.size)
        assertEquals("encoded-cursor", page.nextCursor)

        val encodedCursor = cursorSlot.captured
        assertEquals(300L, encodedCursor.sessionMaxId)
        assertEquals(baseCursor.windowAnchorId, encodedCursor.windowAnchorId) // windowAnchorId는 변경되지 않음
        assertEquals(555, encodedCursor.lastShuffleKey)
        assertEquals(20L, encodedCursor.lastUkId)
    }

    @Test
    fun `description이 null이면 빈 문자열로 매핑된다`() {
        val feedWithNullDescription = testFeed(1L).copy(description = Description(null))
        val result = FeedWindowResult(
            windowFetchedCount = 1,
            sessionMaxId = 100L,
            feeds = listOf(feedWithNullDescription),
            windowMinUkId = 1L,
            lastShuffleKey = 1,
            lastUkId = 1L,
        )

        val page = result.toCursorPage(baseCursor, limit = 10)

        assertEquals("", page.items.single().description)
    }

    @Test
    fun `실제 FeedCursorCodec으로 인코딩한 커서는 다시 디코딩할 수 있다`() {
        // mockkObject를 사용하지 않고 실제 인코딩-디코딩 라운드트립을 검증
        val feeds = listOf(testFeed(1L))
        val result = FeedWindowResult(
            windowFetchedCount = 1,
            sessionMaxId = 400L,
            feeds = feeds,
            windowMinUkId = 1L,
            lastShuffleKey = 100,
            lastUkId = 1L,
        )

        val page = result.toCursorPage(baseCursor, limit = 10)

        val decoded = FeedCursorCodec.decodeOrNull(page.nextCursor)
        assertEquals(400L, decoded?.sessionMaxId)
        assertEquals(1L, decoded?.windowAnchorId)
    }
}
