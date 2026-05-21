package com.turnin.domain.feed.application.dto

import kotlinx.serialization.Serializable

/**
 * 피드 조회에 필요한 커서
 *
 * @property score 점수 커서
 * @property userKeywordId 사용자 키워드 ID 커서
 */
@Serializable
data class FeedCursor(
    val score: Double,
    val userKeywordId: Long,
)
