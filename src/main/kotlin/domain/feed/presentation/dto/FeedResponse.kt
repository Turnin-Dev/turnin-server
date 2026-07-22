package com.turnin.domain.feed.presentation.dto

import com.turnin.common.util.pagination.cursor.CursorPage
import com.turnin.domain.feed.application.dto.FeedCursor
import com.turnin.domain.feed.application.dto.FeedCursorCodec
import com.turnin.domain.feed.application.dto.FeedDto
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
                )
            },
            nextCursor = FeedCursorCodec.encode(
                FeedCursor(
                    seed = "sample-seed-1234",
                    sessionMaxId = 100L,
                    windowAnchorId = null,
                    lastShuffleKey = 4213,
                    lastUkId = 2L,
                ),
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
    )
