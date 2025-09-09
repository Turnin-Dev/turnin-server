package com.peekr.domain.keyword.presentation.dto

import com.peekr.domain.keyword.application.dto.UserKeywordDto
import kotlinx.serialization.Serializable

/**
 * 사용자별 키워드 응답 바디
 *
 * @property id 사용자별 키워드 ID
 * @property keywordId 키워드 ID
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
    val userId: Long,
    val offsetX: Float,
    val offsetY: Float,
    val description: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

fun UserKeywordDto.toResponse() = UserKeywordResponse(
    id = id,
    keywordId = keywordId,
    userId = userId,
    offsetX = offsetX,
    offsetY = offsetY,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
