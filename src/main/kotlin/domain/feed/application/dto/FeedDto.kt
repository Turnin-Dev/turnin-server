package com.peekr.domain.feed.application.dto

import com.peekr.domain.feed.domain.model.Feed

/**
 * 피드 DTO
 *
 * @property userKeywordId 사용자 키워드 ID
 * @property userId 사용자 ID
 * @property userName 사용자 명
 * @property profileImageUrl 프로필 사진 URL
 * @property keywordId 키워드 ID
 * @property keyword 키워드 명
 * @property description 키워드 내용
 * @property createdAt 키워드 생성 일자
 * @property score 피드 점수(피드 표시 조건을 위한 점수, 높을수록 피드가 표시될 확률이 높음)
 * @property similarity 유사도(사용자의 키워드들과 유사한 정도를 나타냄, 1.0에 가까울수록 유사함)
 */
data class FeedDto(
    val userKeywordId: Long,
    val userId: Long,
    val userName: String,
    val profileImageUrl: String?,
    val keywordId: Long,
    val keyword: String,
    val description: String,
    val createdAt: Long,
    val score: Double,
    val similarity: Double,
)

fun Feed.toDto(): FeedDto =
    FeedDto(
        userKeywordId = userKeywordId.value,
        userId = userId.value,
        userName = userName.value,
        profileImageUrl = profileImageUrl,
        keywordId = keywordId.value,
        keyword = keyword.value,
        description = description.value ?: "",
        createdAt = createdAt,
        score = score,
        similarity = similarity,
    )

fun FeedDto.toCursor(): FeedCursor =
    FeedCursor(
        score = score,
        createdAt = createdAt,
        userKeywordId = userKeywordId,
    )
