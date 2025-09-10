package com.peekr.domain.keyword.application.dto

import com.peekr.domain.keyword.domain.model.UserKeyword
import com.peekr.domain.keyword.presentation.dto.UserKeywordResponse
import kotlinx.serialization.Serializable

/**
 * 사용자별 키워드 DTO
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
data class UserKeywordDto(
    val id: Long,
    val keywordId: Long,
    val userId: Long,
    val offsetX: Float,
    val offsetY: Float,
    val description: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

fun UserKeyword.toDto(): UserKeywordDto = UserKeywordDto(
    id = id.value,
    keywordId = keywordId.value,
    userId = userId.value,
    offsetX = offsetX,
    offsetY = offsetY,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun List<UserKeyword>.toDto(): List<UserKeywordDto> = map { it.toDto() }

fun UserKeywordDto.toResponse(): UserKeywordResponse = UserKeywordResponse(
    id = this.id,
    keywordId = this.keywordId,
    userId = this.userId,
    offsetX = this.offsetX,
    offsetY = this.offsetY,
    description = this.description,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
)
