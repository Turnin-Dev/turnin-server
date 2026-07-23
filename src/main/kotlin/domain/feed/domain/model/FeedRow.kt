package com.turnin.domain.feed.domain.model

/**
 * 피드와 추가 필드가 포함된 모델
 *
 * @param feed 피드
 * @param shuffleKey 셔플 키 (커서로 사용됨)
 */
data class FeedRow(
    val feed: Feed,
    val shuffleKey: Int,
)
