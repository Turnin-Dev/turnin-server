package com.peekr.domain.userKeyword.application.dto

import com.peekr.domain.userKeyword.domain.model.UserKeyword
import com.peekr.domain.userKeyword.domain.model.toDto
import com.peekr.domain.userKeyword.presentation.dto.UserKeywordResponse

/**
 * 사용자별 키워드 DTO
 *
 * @property id 사용자별 키워드 ID
 * @property keywordId 키워드 ID
 * @property keywordName 키워드 명
 * @property userId 사용자 ID
 * @property offset UI 좌표 상에서의 위치 오프셋 값
 * @property description 키워드 개인 설명
 * @property createdAt 생성 일자
 * @property updatedAt 수정 일자
 */
data class UserKeywordDto(
    val id: Long,
    val keywordId: Long,
    val keywordName: String,
    val userId: Long,
    val offset: OffsetDto,
    val description: DescriptionDto?,
    val createdAt: Long,
    val updatedAt: Long,
)

fun UserKeyword.toDto(keywordName: String): UserKeywordDto = UserKeywordDto(
    id = id.value,
    keywordId = keywordId.value,
    keywordName = keywordName,
    userId = userId.value,
    offset = offset.toDto(),
    description = description?.toDto(),
    createdAt = createdAt,
    updatedAt = updatedAt,
)

fun UserKeywordDto.toResponse(): UserKeywordResponse = UserKeywordResponse(
    id = this.id,
    keywordId = this.keywordId,
    keywordName = this.keywordName,
    userId = this.userId,
    offsetX = this.offset.x,
    offsetY = this.offset.y,
    description = this.description?.value,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
)
