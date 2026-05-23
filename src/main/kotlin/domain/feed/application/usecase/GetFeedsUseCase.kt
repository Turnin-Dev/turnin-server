package com.turnin.domain.feed.application.usecase

import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.common.util.pagination.cursor.CursorPage
import com.turnin.domain.feed.application.dto.FeedCursor
import com.turnin.domain.feed.application.dto.FeedDto
import com.turnin.domain.feed.application.dto.toCursor
import com.turnin.domain.feed.application.dto.toDto
import com.turnin.domain.feed.domain.repository.FeedRepository

/**
 * 피드 조회
 *
 * @see invoke
 */
class GetFeedsUseCase(private val feedRepository: FeedRepository) {
    /**
     * ## 피드 조회
     * 피드를 커서 기반 페이지네이션을 사용하여 조회한다.
     *
     * ### 피드 조회 조건
     * 1. 친구 키워드
     * 2. 유사 키워드
     * ### 기본 조건
     * 1. 최신 순 정렬
     * 2. 비활성화, 차단 사용자 필터링
     *
     * @param userId 조회할 사용자 ID
     * @param cursor 피드 커서 값
     * @param pageSize 페이지 사이즈
     */
    suspend operator fun invoke(
        userId: Long,
        cursor: FeedCursor?,
        pageSize: Int,
    ): CursorPage<FeedDto, FeedCursor> {
        // 0) 데이터 전처리
        val userIdVO = UserId(userId)
        val cursorUkIdVO: UserKeywordId? = cursor?.userKeywordId?.let { UserKeywordId(it) }

        // 1) 피드 목록 조회 (폴백 전환 여부에 따라 쿼리 분기)
        val isFallback = cursor?.score != null && cursor.score == 0.0
        val feedsWithOneExtra = if (isFallback) {
            feedRepository.getFallbackFeeds(
                userId = userIdVO,
                cursorUkId = cursorUkIdVO,
                limit = pageSize + 1,
            )
        } else {
            feedRepository.getFeeds(
                userId = userIdVO,
                cursorScore = cursor?.score,
                cursorUkId = cursorUkIdVO,
                limit = pageSize + 1,
            )
        }

        // 2) 다음 페이지 존재 여부 확인 및 반환할 피드 정제
        val hasNext = feedsWithOneExtra.size > pageSize
        val feeds = if (hasNext) feedsWithOneExtra.take(pageSize) else feedsWithOneExtra
        val feedsDto = feeds.map { it.toDto() }

        // 3) 다음 커서 결정
        val nextCursor = if (hasNext) feedsDto.last().toCursor() else null

        // 4) 최종 반환
        return CursorPage(
            items = feedsDto,
            nextCursor = nextCursor,
        )
    }
}
