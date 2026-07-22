package com.turnin.domain.feed.application.usecase

import com.turnin.common.util.pagination.cursor.CursorPage
import com.turnin.domain.feed.application.dto.FeedCursor
import com.turnin.domain.feed.application.dto.FeedCursorCodec
import com.turnin.domain.feed.application.dto.FeedDto
import com.turnin.domain.feed.application.dto.toDto
import com.turnin.domain.feed.domain.model.FeedWindowResult

/**
 * [FeedWindowResult]를 다음 요청용 cursor가 포함된 [CursorPage]로 변환한다.
 *
 * ## 판단 기준
 * - `windowFetchedCount == 0`: 현재 청크(window_pool) 자체가 비어있음 → 더 이상 조회할 피드가 없는 것으로 간주하고 종료(`nextCursor = null`).
 *   (해당 청크 구간의 글이 전부 비활성화/차단 등으로 빠진 경우, 그보다 과거 구간에는 여전히 피드가 남아있을 수 있으나
 *   이 시점엔 windowMinUkId도 null이라 다음 anchor를 잡을 수 없으므로 종료 처리함)
 * - `feeds.size < limit`: 커서(lastShuffleKey, lastUkId) 이후로 현재 청크 안에 더 볼 행이 없다는 뜻 →
 *   청크가 소진된 것으로 보고 windowAnchorId를 windowMinUkId로 옮기고, 청크 내부 커서(lastShuffleKey, lastUkId)는 리셋
 * - `feeds.size == limit`: 같은 청크 안에 더 있을 가능성이 있으므로 windowAnchorId는 유지하고
 *   청크 내부 커서만 이번 응답의 마지막 행 값(lastShuffleKey, lastUkId)으로 갱신
 *
 * sessionMaxId는 매 페이지 응답에 항상 함께 실어 커서에 고정 반영한다 (세션 시작 이후 새 글 유입 차단용 스냅샷이므로 절대 덮어쓰면 안 됨).
 *
 * @param cursor 이번 조회에 사용됐던 요청 cursor. 다음 cursor를 만들 때 seed 등 불변 필드를 그대로 이어받기 위해 필요
 * @param limit 이번 조회에 사용됐던 페이지 크기. 청크 소진 여부(feeds.size < limit) 판단 기준
 */
fun FeedWindowResult.toCursorPage(cursor: FeedCursor, limit: Int): CursorPage<FeedDto, String> {
    if (windowFetchedCount == 0) {
        return CursorPage(items = emptyList(), nextCursor = null)
    }

    val withSessionMaxId = cursor.copy(sessionMaxId = sessionMaxId)

    val nextCursor = if (feeds.size < limit) {
        withSessionMaxId.copy(
            windowAnchorId = windowMinUkId,
            lastShuffleKey = null,
            lastUkId = null,
        )
    } else {
        withSessionMaxId.copy(
            lastShuffleKey = lastShuffleKey,
            lastUkId = lastUkId,
        )
    }

    return CursorPage(
        items = feeds.map { it.toDto() },
        nextCursor = FeedCursorCodec.encode(nextCursor),
    )
}
