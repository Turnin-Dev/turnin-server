package com.peekr.domain.feed.presentation.dto

import com.peekr.common.util.pagination.cursor.CursorPage
import com.peekr.domain.feed.application.dto.FeedCursor
import com.peekr.domain.feed.application.dto.FeedDto
import kotlinx.serialization.Serializable

/**
 * 피드 응답 모델
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
@Serializable
data class FeedResponse(
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
) {
    companion object {
        val sample = CursorPage(
            items = List(2) {
                FeedResponse(
                    userKeywordId = (it + 1).toLong(),
                    userId = (it + 1).toLong(),
                    userName = "username",
                    profileImageUrl = "https://image-server-1.com/image$it.jpg",
                    keywordId = (it + 1).toLong(),
                    keyword = "keyword",
                    description = "description",
                    createdAt = 1000L,
                    score = 50.0,
                    similarity = 0.9,
                )
            },
            nextCursor = FeedCursor(
                score = 50.0,
                createdAt = 1000L,
                userKeywordId = 2L,
            ),
        )
    }
}

fun FeedDto.toResponse(): FeedResponse =
    FeedResponse(
        userKeywordId = userKeywordId,
        userId = userId,
        userName = userName,
        profileImageUrl = profileImageUrl,
        keywordId = keywordId,
        keyword = keyword,
        description = description,
        createdAt = createdAt,
        score = score,
        similarity = similarity,
    )
