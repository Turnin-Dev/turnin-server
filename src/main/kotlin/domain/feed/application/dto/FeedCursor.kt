package com.peekr.domain.feed.application.dto

/**
 * 피드 조회에 필요한 커서
 *
 * @property score 점수 커서
 * @property createdAt 생성일 커서
 * @property userKeywordId 사용자 키워드 ID 커서
 */
data class FeedCursor(
    val score: Double?,
    val createdAt: Long?,
    val userKeywordId: Long?,
)
