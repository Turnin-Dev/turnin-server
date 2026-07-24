package com.turnin.domain.feed.application.dto

import com.turnin.domain.feed.domain.model.Feed

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
    )
