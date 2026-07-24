package com.turnin.domain.feed.application.usecase

import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.common.util.log.LogAction
import com.turnin.common.util.log.LogTag
import com.turnin.common.util.log.LogType
import com.turnin.common.util.pagination.cursor.CursorPage
import com.turnin.domain.feed.application.dto.FeedCursor
import com.turnin.domain.feed.application.dto.FeedCursorCodec
import com.turnin.domain.feed.application.dto.FeedDto
import com.turnin.domain.feed.application.dto.FeedType
import com.turnin.domain.feed.application.dto.toDto
import com.turnin.domain.feed.domain.repository.FeedRepository

/**
 * # 전체 피드 조회 유스케이스
 *
 * cursor를 디코딩하여 전체 피드의 다음 청크(윈도우)를 조회하고,
 * 청크 소진 여부에 따라 다음 요청에 사용할 cursor를 재발급한다.
 *
 * - cursor가 없으면 (신규 진입 / 새로고침) 새 seed를 발급하고 첫 청크부터 시작한다. 이때 sessionMaxId도 null로 전달되어,
 *   리포지토리가 현재 시점의 MAX(uk_id)를 계산해 응답에 실어 돌려주고 그 값이 이후 모든 페이지의 커서에 고정된다.
 * - cursor 디코딩에 실패하면 손상된 것으로 간주하고 첫 페이지로 취급한다.
 *
 * ##### **[페이지네이션 버그 티켓](https://peekr-app.atlassian.net/browse/PK-147)**
 *
 * @see invoke
 */
class GetFeedsUseCase(
    private val feedRepository: FeedRepository,
    private val windowSize: Int = DEFAULT_WINDOW_SIZE,
) {
    suspend operator fun invoke(
        type: FeedType,
        userId: UserId,
        cursorRaw: String?,
        limit: Int,
    ): CursorPage<FeedDto, String> {
        // 커서 디코딩
        val cursor = FeedCursorCodec.decodeOrNull(cursorRaw) ?: FeedCursor.initial()

        // (페이지 크기 + 1)개의 피드와 그 결과를 조회
        val result = when (type) {
            FeedType.ALL -> {
                feedRepository.getAllFeeds(
                    userId = userId,
                    seed = cursor.seed,
                    sessionMaxId = cursor.sessionMaxId,
                    windowAnchorId = cursor.windowAnchorId?.let { UserKeywordId(it) },
                    lastShuffleKey = cursor.lastShuffleKey,
                    lastUkId = cursor.lastUkId,
                    windowSize = windowSize,
                    limit = limit + 1,
                )
            }

            FeedType.FRIEND -> {
                feedRepository.getFriendFeeds(
                    userId = userId,
                    seed = cursor.seed,
                    sessionMaxId = cursor.sessionMaxId,
                    windowAnchorId = cursor.windowAnchorId?.let { UserKeywordId(it) },
                    lastShuffleKey = cursor.lastShuffleKey,
                    lastUkId = cursor.lastUkId,
                    windowSize = windowSize,
                    limit = limit + 1,
                )
            }
        }
        val feedRowsWithOneExtra = result.feedsRows

        // 윈도우풀 소진 시 전체 종료
        if (result.windowFetchedCount == 0) {
            LOGGER.info(
                message = "All feed window pools have been exhausted.",
                tags = mapOf(
                    LogTag.LOG_TYPE.name to LogType.NORMAL.name,
                    LogTag.ACTION.name to LogAction.FEED_WINDOW_POOL_EXHAUSTION.name,
                ),
            )
            return CursorPage(items = emptyList(), nextCursor = null)
        }

        // 커서에 세션 상한 ID 값 설정
        val cursorWithSessionMaxId = cursor.copy(sessionMaxId = result.sessionMaxId)

        // 다음 페이지 존재 여부 확인, 반환할 피드 정제, 다음 커서 결정
        val hasNext = feedRowsWithOneExtra.size > limit
        val feedRows = if (hasNext) feedRowsWithOneExtra.take(limit) else feedRowsWithOneExtra
        val nextCursor = if (hasNext) {
            cursorWithSessionMaxId.copy(
                lastShuffleKey = feedRows.last().shuffleKey,
                lastUkId = feedRows
                    .last()
                    .feed.userKeywordId.value,
            )
        } else {
            cursorWithSessionMaxId.copy(
                windowAnchorId = result.windowMinUkId,
                lastShuffleKey = null,
                lastUkId = null,
            )
        }

        // 최종 커서 페이지 반환
        return CursorPage(
            items = feedRows.map { it.feed.toDto() },
            nextCursor = FeedCursorCodec.encode(nextCursor),
        )
    }

    companion object {
        private const val DEFAULT_WINDOW_SIZE = 1000
    }
}

private val LOGGER = AppLoggerFactory.createLogger<GetFeedsUseCase>()
