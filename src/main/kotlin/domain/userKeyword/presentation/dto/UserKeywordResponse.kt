package com.peekr.domain.userKeyword.presentation.dto

import com.peekr.domain.userKeyword.application.dto.UserKeywordDto
import kotlinx.serialization.Serializable

/**
 * 사용자별 키워드 응답 바디
 *
 * @property id 사용자별 키워드 ID
 * @property keywordId 키워드 ID
 * @property keywordName 키워드 명
 * @property userId 사용자 ID
 * @property offsetX UI 좌표 상에서의 X 위치
 * @property offsetY UI 좌표 상에서의 Y 위치
 * @property description 키워드 개인 설명
 * @property createdAt 생성 일자
 * @property updatedAt 수정 일자
 */
@Serializable
data class UserKeywordResponse(
    val id: Long,
    val keywordId: Long,
    val keywordName: String,
    val userId: Long,
    val offsetX: Float,
    val offsetY: Float,
    val description: String?,
    val createdAt: Long,
    val updatedAt: Long,
) {
    companion object {
        val sample = UserKeywordResponse(
            id = 1,
            keywordId = 1,
            keywordName = "sample",
            userId = 1,
            offsetX = 0.25f,
            offsetY = 0.25f,
            description = "",
            createdAt = 0,
            updatedAt = 0,
        )
    }
}

fun UserKeywordDto.toResponse() = UserKeywordResponse(
    id = id,
    keywordId = keywordId,
    keywordName = keywordName,
    userId = userId,
    offsetX = offset.x,
    offsetY = offset.y,
    description = description?.value,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
