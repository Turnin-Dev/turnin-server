package com.turnin.domain.feed.application.usecase

import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.common.util.pagination.cursor.CursorPage
import com.turnin.domain.feed.application.dto.FeedCursor
import com.turnin.domain.feed.application.dto.FeedCursorCodec
import com.turnin.domain.feed.application.dto.FeedDto
import com.turnin.domain.feed.domain.repository.FeedRepository

/**
 * # 친구 피드 조회 유스케이스
 *
 * cursor를 디코딩하여 친구 피드의 다음 청크(윈도우)를 조회하고,
 * 청크 소진 여부에 따라 다음 요청에 사용할 cursor를 재발급한다.
 *
 * - cursor가 없으면 (신규 진입 / 새로고침) 새 seed를 발급하고 첫 청크부터 시작한다. 이때 sessionMaxId도 null로 전달되어,
 *   리포지토리가 현재 시점의 MAX(uk_id)를 계산해 응답에 실어 돌려주고 그 값이 이후 모든 페이지의 커서에 고정된다.
 * - cursor 디코딩에 실패하면 손상된 것으로 간주하고 첫 페이지로 취급한다.
 * - 실제 다음 페이지/청크 전환 판단은 [FeedWindowResult.toCursorPage]에 위임한다.
 *
 * @see invoke
 */
class GetFriendFeedsUseCase(
    private val feedRepository: FeedRepository,
    private val windowSize: Int = DEFAULT_WINDOW_SIZE,
) {
    suspend operator fun invoke(
        userId: UserId,
        cursorRaw: String?,
        limit: Int,
    ): CursorPage<FeedDto, String> {
        val cursor = FeedCursorCodec.decodeOrNull(cursorRaw) ?: FeedCursor.initial()

        val result = feedRepository.getFriendFeeds(
            userId = userId,
            seed = cursor.seed,
            sessionMaxId = cursor.sessionMaxId,
            windowAnchorId = cursor.windowAnchorId?.let { UserKeywordId(it) },
            lastShuffleKey = cursor.lastShuffleKey,
            lastUkId = cursor.lastUkId,
            windowSize = windowSize,
            limit = limit,
        )

        return result.toCursorPage(cursor, limit)
    }

    companion object {
        private const val DEFAULT_WINDOW_SIZE = 3000
    }
}
