package com.turnin.domain.feed.application.usecase

import com.turnin.common.model.KeywordName
import com.turnin.common.model.UserName
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.common.util.pagination.cursor.CursorCodec
import com.turnin.domain.feed.application.dto.FeedCursor
import com.turnin.domain.feed.application.dto.FeedType
import com.turnin.domain.feed.application.dto.toDto
import com.turnin.domain.feed.domain.model.Feed
import com.turnin.domain.feed.domain.model.FeedRow
import com.turnin.domain.feed.domain.model.FeedWindowResult
import com.turnin.domain.feed.domain.repository.FeedRepository
import com.turnin.domain.userKeyword.domain.model.Description
import io.mockk.MockKMatcherScope
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class GetFeedsUseCaseTest {
    private val feedRepository = mockk<FeedRepository>()
    private val usecase = GetFeedsUseCase(feedRepository)

    // ------------------------------ 공통 흐름 ------------------------------

    @Test
    fun `ALL - cursor가 없으면 새 seed를 발급해 첫 청크를 (limit+1로) 요청한다`() = runTest {
        checkFirstPageWithoutCursor(FeedType.ALL)
    }

    @Test
    fun `FRIEND - cursor가 없으면 새 seed를 발급해 첫 청크를 (limit+1로) 요청한다`() = runTest {
        checkFirstPageWithoutCursor(FeedType.FRIEND)
    }

    private suspend fun checkFirstPageWithoutCursor(type: FeedType) {
        // given
        val seedSlot = slot<String>()
        stub(
            type = type,
            seed = { capture(seedSlot) },
            sessionMaxId = null,
            windowAnchorId = null,
            lastShuffleKey = null,
            lastUkId = null,
            windowSize = DEFAULT_WINDOW_SIZE,
            limit = TEST_LIMIT + 1,
            result = TestWindowResultNoNext,
        )

        // when
        val page = usecase(type, TestUserId, cursorRaw = null, limit = TEST_LIMIT)

        // then
        assertTrue(seedSlot.captured.isNotBlank())
        assertEquals(TestWindowResultNoNext.feedsRows.map { it.feed.toDto() }, page.items)
        assertNotNull(page.nextCursor)
    }

    @Test
    fun `ALL - 유효한 cursor가 있으면 디코딩한 값으로 다음 청크를 (limit+1로) 요청한다`() = runTest {
        checkNextPageWithCursor(FeedType.ALL)
    }

    @Test
    fun `FRIEND - 유효한 cursor가 있으면 디코딩한 값으로 다음 청크를 (limit+1로) 요청한다`() = runTest {
        checkNextPageWithCursor(FeedType.FRIEND)
    }

    private suspend fun checkNextPageWithCursor(type: FeedType) {
        // given
        val cursor = FeedCursor(
            seed = "existing-seed",
            sessionMaxId = 500L,
            windowAnchorId = 42L,
            lastShuffleKey = 7,
            lastUkId = 20L,
        )
        val cursorRaw = CursorCodec.encode(cursor)

        stub(
            type = type,
            seed = { "existing-seed" },
            sessionMaxId = 500L,
            windowAnchorId = 42L,
            lastShuffleKey = 7,
            lastUkId = 20L,
            windowSize = DEFAULT_WINDOW_SIZE,
            limit = TEST_LIMIT + 1,
            result = TestWindowResultNoNext,
        )

        // when
        val page = usecase(type, TestUserId, cursorRaw = cursorRaw, limit = TEST_LIMIT)

        // then
        verifyCalled(
            type = type,
            seed = { "existing-seed" },
            sessionMaxId = 500L,
            windowAnchorId = 42L,
            lastShuffleKey = 7,
            lastUkId = 20L,
            windowSize = DEFAULT_WINDOW_SIZE,
            limit = TEST_LIMIT + 1,
        )
        assertEquals(TestWindowResultNoNext.feedsRows.map { it.feed.toDto() }, page.items)
    }

    @Test
    fun `ALL - cursor 디코딩에 실패하면 첫 페이지로 취급한다`() = runTest {
        checkInvalidCursorFallsBackToFirstPage(FeedType.ALL)
    }

    @Test
    fun `FRIEND - cursor 디코딩에 실패하면 첫 페이지로 취급한다`() = runTest {
        checkInvalidCursorFallsBackToFirstPage(FeedType.FRIEND)
    }

    private suspend fun checkInvalidCursorFallsBackToFirstPage(type: FeedType) {
        // given
        stub(
            type = type,
            seed = { any() },
            sessionMaxId = null,
            windowAnchorId = null,
            lastShuffleKey = null,
            lastUkId = null,
            windowSize = DEFAULT_WINDOW_SIZE,
            limit = TEST_LIMIT + 1,
            result = TestWindowResultNoNext,
        )

        // when
        val page = usecase(type, TestUserId, cursorRaw = "not-a-valid-cursor!!", limit = TEST_LIMIT)

        // then
        assertEquals(TestWindowResultNoNext.feedsRows.map { it.feed.toDto() }, page.items)
    }

    @Test
    fun `ALL - 생성자에 windowSize를 지정하면 해당 값이 리포지토리에 전달된다`() = runTest {
        checkCustomWindowSize(FeedType.ALL)
    }

    @Test
    fun `FRIEND - 생성자에 windowSize를 지정하면 해당 값이 리포지토리에 전달된다`() = runTest {
        checkCustomWindowSize(FeedType.FRIEND)
    }

    private suspend fun checkCustomWindowSize(type: FeedType) {
        // given
        val customUsecase = GetFeedsUseCase(feedRepository, windowSize = CUSTOM_WINDOW_SIZE)
        stub(
            type = type,
            seed = { any() },
            sessionMaxId = null,
            windowAnchorId = null,
            lastShuffleKey = null,
            lastUkId = null,
            windowSize = CUSTOM_WINDOW_SIZE,
            limit = TEST_LIMIT + 1,
            result = TestWindowResultNoNext,
        )

        // when
        customUsecase(type, TestUserId, cursorRaw = null, limit = TEST_LIMIT)

        // then
        verifyCalled(
            type = type,
            seed = { any() },
            sessionMaxId = null,
            windowAnchorId = null,
            lastShuffleKey = null,
            lastUkId = null,
            windowSize = CUSTOM_WINDOW_SIZE,
            limit = TEST_LIMIT + 1,
        )
    }

    @Test
    fun `ALL - windowFetchedCount가 0이면 빈 목록과 null 커서를 반환한다`() = runTest {
        checkEmptyChunkReturnsEmptyPage(FeedType.ALL)
    }

    @Test
    fun `FRIEND - windowFetchedCount가 0이면 빈 목록과 null 커서를 반환한다`() = runTest {
        checkEmptyChunkReturnsEmptyPage(FeedType.FRIEND)
    }

    private suspend fun checkEmptyChunkReturnsEmptyPage(type: FeedType) {
        // given
        stub(
            type = type,
            seed = { any() },
            sessionMaxId = null,
            windowAnchorId = null,
            lastShuffleKey = null,
            lastUkId = null,
            windowSize = DEFAULT_WINDOW_SIZE,
            limit = TEST_LIMIT + 1,
            result = TestEmptyWindowResult,
        )

        // when
        val page = usecase(type, TestUserId, cursorRaw = null, limit = TEST_LIMIT)

        // then
        assertTrue(page.items.isEmpty())
        assertNull(page.nextCursor)
    }

    // ------------------------------ toCursorPage 통합 이후 로직 (sentinel 기반 hasNext 판단) ------------------------------

    @Test
    fun `ALL - hasNext면 limit개로 잘리고 anchor는 유지된 채 커서는 trim된 마지막 행 기준으로 갱신된다`() = runTest {
        checkHasNextUsesTrimmedLastRow(FeedType.ALL)
    }

    @Test
    fun `FRIEND - hasNext면 limit개로 잘리고 anchor는 유지된 채 커서는 trim된 마지막 행 기준으로 갱신된다`() = runTest {
        checkHasNextUsesTrimmedLastRow(FeedType.FRIEND)
    }

    private suspend fun checkHasNextUsesTrimmedLastRow(type: FeedType) {
        // given: limit+1(21)개가 반환됨 → 같은 청크에 아직 더 남아있는 상황
        val rowsWithExtra = (1..TEST_LIMIT + 1).map { i -> testFeedRow(ukId = 200L + i, shuffleKey = 100 + i) }
        val windowResult = FeedWindowResult(
            feedsRows = rowsWithExtra,
            windowMinUkId = 150L,
            windowFetchedCount = rowsWithExtra.size,
            sessionMaxId = 999L,
        )
        val existingCursor = FeedCursor(
            seed = "existing-seed",
            sessionMaxId = null,
            windowAnchorId = 42L,
            lastShuffleKey = null,
            lastUkId = null,
        )
        val cursorRaw = CursorCodec.encode(existingCursor)

        stub(
            type = type,
            seed = { "existing-seed" },
            sessionMaxId = null,
            windowAnchorId = 42L,
            lastShuffleKey = null,
            lastUkId = null,
            windowSize = DEFAULT_WINDOW_SIZE,
            limit = TEST_LIMIT + 1,
            result = windowResult,
        )

        // when
        val page = usecase(type, TestUserId, cursorRaw = cursorRaw, limit = TEST_LIMIT)

        // then: limit개로 trim
        assertEquals(TEST_LIMIT, page.items.size)
        assertEquals(rowsWithExtra.take(TEST_LIMIT).map { it.feed.toDto() }, page.items)

        val nextCursor = requireNotNull(CursorCodec.decodeOrNull<FeedCursor>(page.nextCursor))
        val trimmedLast = rowsWithExtra[TEST_LIMIT - 1] // 20번째(index 19) = 실제로 보여준 마지막 행
        val extraRow = rowsWithExtra[TEST_LIMIT] // 21번째(index 20) = sentinel용 extra row

        // 회귀 검증: 커서가 extra row가 아니라 trim된 마지막 행 값을 기준으로 잡혀야 함
        assertEquals(trimmedLast.feed.userKeywordId.value, nextCursor.lastUkId)
        assertEquals(trimmedLast.shuffleKey, nextCursor.lastShuffleKey)
        assertNotEquals(extraRow.feed.userKeywordId.value, nextCursor.lastUkId)
        assertNotEquals(extraRow.shuffleKey, nextCursor.lastShuffleKey)

        // 같은 청크가 아직 안 끝났으므로 windowAnchorId는 그대로 유지되어야 함
        assertEquals(existingCursor.windowAnchorId, nextCursor.windowAnchorId)
    }

    @Test
    fun `ALL - 청크 크기가 limit의 배수여도 sentinel로 정확히 소진을 판단해 anchor를 이동시킨다 (구버전 조기종료 버그 회귀)`() = runTest {
        checkChunkExactlyExhausted(FeedType.ALL)
    }

    @Test
    fun `FRIEND - 청크 크기가 limit의 배수여도 sentinel로 정확히 소진을 판단해 anchor를 이동시킨다 (구버전 조기종료 버그 회귀)`() = runTest {
        checkChunkExactlyExhausted(FeedType.FRIEND)
    }

    private suspend fun checkChunkExactlyExhausted(type: FeedType) {
        // given: limit+1(21)개를 요청했지만 청크에 정확히 limit(20)개만 남아있던 상황
        // (구버전 로직이었다면 feeds.size == limit로 오판해 anchor를 유지 → 다음 요청에서 0행 → 조기 종료되던 케이스)
        val exactlyLimitRows = (1..TEST_LIMIT).map { i -> testFeedRow(ukId = 200L + i, shuffleKey = 100 + i) }
        val windowResult = FeedWindowResult(
            feedsRows = exactlyLimitRows,
            windowMinUkId = 201L,
            windowFetchedCount = exactlyLimitRows.size,
            sessionMaxId = 999L,
        )
        val existingCursor = FeedCursor(
            seed = "existing-seed",
            sessionMaxId = null,
            windowAnchorId = 42L,
            lastShuffleKey = null,
            lastUkId = null,
        )
        val cursorRaw = CursorCodec.encode(existingCursor)

        stub(
            type = type,
            seed = { "existing-seed" },
            sessionMaxId = null,
            windowAnchorId = 42L,
            lastShuffleKey = null,
            lastUkId = null,
            windowSize = DEFAULT_WINDOW_SIZE,
            limit = TEST_LIMIT + 1,
            result = windowResult,
        )

        // when
        val page = usecase(type, TestUserId, cursorRaw = cursorRaw, limit = TEST_LIMIT)

        // then: 응답 자체는 정상 반환 (조기 종료되면 안 됨)
        assertEquals(TEST_LIMIT, page.items.size)
        assertEquals(exactlyLimitRows.map { it.feed.toDto() }, page.items)

        // 청크가 정확히 소진됐으므로 anchor는 windowMinUkId로 이동, 내부 커서는 리셋되어야 함
        val nextCursor = requireNotNull(CursorCodec.decodeOrNull<FeedCursor>(page.nextCursor))
        assertEquals(windowResult.windowMinUkId, nextCursor.windowAnchorId)
        assertNull(nextCursor.lastShuffleKey)
        assertNull(nextCursor.lastUkId)
    }

    // ------------------------------ 스텁/검증/픽스처 헬퍼 ------------------------------

    private fun stub(
        type: FeedType,
        seed: MockKMatcherScope.() -> String,
        sessionMaxId: Long?,
        windowAnchorId: Long?,
        lastShuffleKey: Int?,
        lastUkId: Long?,
        windowSize: Int,
        limit: Int,
        result: FeedWindowResult,
    ) {
        when (type) {
            FeedType.ALL -> coEvery {
                feedRepository.getAllFeeds(
                    userId = TestUserId,
                    seed = seed(),
                    sessionMaxId = sessionMaxId,
                    windowAnchorId = windowAnchorId?.let { UserKeywordId(it) },
                    lastShuffleKey = lastShuffleKey,
                    lastUkId = lastUkId,
                    windowSize = windowSize,
                    limit = limit,
                )
            } returns result

            FeedType.FRIEND -> coEvery {
                feedRepository.getFriendFeeds(
                    userId = TestUserId,
                    seed = seed(),
                    sessionMaxId = sessionMaxId,
                    windowAnchorId = windowAnchorId?.let { UserKeywordId(it) },
                    lastShuffleKey = lastShuffleKey,
                    lastUkId = lastUkId,
                    windowSize = windowSize,
                    limit = limit,
                )
            } returns result
        }
    }

    private fun verifyCalled(
        type: FeedType,
        seed: MockKMatcherScope.() -> String,
        sessionMaxId: Long?,
        windowAnchorId: Long?,
        lastShuffleKey: Int?,
        lastUkId: Long?,
        windowSize: Int,
        limit: Int,
    ) {
        when (type) {
            FeedType.ALL -> coVerify(exactly = 1) {
                feedRepository.getAllFeeds(
                    userId = TestUserId,
                    seed = seed(),
                    sessionMaxId = sessionMaxId,
                    windowAnchorId = windowAnchorId?.let { UserKeywordId(it) },
                    lastShuffleKey = lastShuffleKey,
                    lastUkId = lastUkId,
                    windowSize = windowSize,
                    limit = limit,
                )
            }

            FeedType.FRIEND -> coVerify(exactly = 1) {
                feedRepository.getFriendFeeds(
                    userId = TestUserId,
                    seed = seed(),
                    sessionMaxId = sessionMaxId,
                    windowAnchorId = windowAnchorId?.let { UserKeywordId(it) },
                    lastShuffleKey = lastShuffleKey,
                    lastUkId = lastUkId,
                    windowSize = windowSize,
                    limit = limit,
                )
            }
        }
    }

    private fun testFeedRow(ukId: Long, shuffleKey: Int) = FeedRow(
        feed = Feed(
            userKeywordId = UserKeywordId(ukId),
            userId = UserId(2L),
            userName = UserName("friend"),
            profileImageUrl = null,
            keywordId = KeywordId(3L),
            keyword = KeywordName("kotlin"),
            description = Description("설명"),
            createdAt = 1_700_000_000_000L,
        ),
        shuffleKey = shuffleKey,
    )

    companion object {
        private const val DEFAULT_WINDOW_SIZE = 1000
        private const val CUSTOM_WINDOW_SIZE = 100
        private const val TEST_LIMIT = 20

        private val TestUserId = UserId(1L)

        private val TestFeedRow = FeedRow(
            feed = Feed(
                userKeywordId = UserKeywordId(10L),
                userId = UserId(2L),
                userName = UserName("friend"),
                profileImageUrl = null,
                keywordId = KeywordId(3L),
                keyword = KeywordName("kotlin"),
                description = Description("설명"),
                createdAt = 1_700_000_000_000L,
            ),
            shuffleKey = 5,
        )

        // 단일 행만 오는, hasNext=false인 일반적인 응답 (limit보다 훨씬 적게 옴)
        private val TestWindowResultNoNext = FeedWindowResult(
            feedsRows = listOf(TestFeedRow),
            windowMinUkId = 10L,
            windowFetchedCount = 1,
            sessionMaxId = 999L,
        )

        private val TestEmptyWindowResult = FeedWindowResult(
            feedsRows = emptyList(),
            windowMinUkId = null,
            windowFetchedCount = 0,
            sessionMaxId = null,
        )
    }
}
