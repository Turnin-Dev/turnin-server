package com.turnin.domain.feed.domain.model

/**
 * 피드 청크(윈도우) 단위 조회 결과.
 *
 * @param feeds 이번 요청으로 조회된 피드 항목 리스트
 * @param windowMinUkId 청크에서 조회된 항목 중 최소 uk_id. 청크 소진 시 다음 windowAnchorId로 사용됨
 * @param windowFetchedCount 청크에서 실제로 조회된 총 개수. windowOffset이 이 값에 도달하면 청크가 소진된 것으로 판단
 * @param sessionMaxId 청크 상한 값. 첫 요청 응답에서 받아 다음 커서에 고정
 * @param lastShuffleKey 다음 페이지 커서
 * @param lastUkId 다음 페이지 커서
 */
data class FeedWindowResult(
    val feeds: List<Feed>,
    val windowMinUkId: Long?,
    val windowFetchedCount: Int,
    val sessionMaxId: Long?,
    val lastShuffleKey: Int?,
    val lastUkId: Long?,
)
