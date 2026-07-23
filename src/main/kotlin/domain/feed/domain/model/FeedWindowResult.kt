package com.turnin.domain.feed.domain.model

/**
 * 피드 청크(윈도우) 단위 조회 결과.
 *
 * @param feedsRows 이번 요청으로 조회된 FeedRow 항목 리스트
 * @param windowMinUkId 청크에서 조회된 항목 중 최소 uk_id. 청크 소진 시 다음 windowAnchorId로 사용됨
 * @param windowFetchedCount 청크 자체가 완전히 비었는지 판단하는 값. 0이면 더 조회할 데이터가 아예 없다는 뜻으로 종료 처리
 * @param sessionMaxId 청크 상한 값. 첫 요청 응답에서 받아 다음 커서에 고정
 */
data class FeedWindowResult(
    val feedsRows: List<FeedRow>,
    val windowMinUkId: Long?,
    val windowFetchedCount: Int,
    val sessionMaxId: Long?,
)
