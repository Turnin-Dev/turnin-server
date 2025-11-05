package com.peekr.domain.userKeyword.application.dto

import com.peekr.domain.userKeyword.domain.model.UserKeyword

/**
 * 사용자별 키워드 DTO
 *
 * @property id 사용자별 키워드 ID
 * @property keywordId 키워드 ID
 * @property keywordName 키워드 명
 * @property userId 사용자 ID
 * @property offset UI 좌표 상에서의 위치 오프셋 값
 * @property createdAt 생성 일자
 * @property updatedAt 수정 일자
 */
data class UserKeywordDto(
    val id: Long,
    val keywordId: Long,
    val keywordName: String,
    val userId: Long,
    val offset: OffsetDto,
    val createdAt: Long,
    val updatedAt: Long,
)

fun UserKeyword.toDto(keywordName: String): UserKeywordDto = UserKeywordDto(
    id = id.value,
    keywordId = keywordId.value,
    keywordName = keywordName,
    userId = userId.value,
    offset = offset.toDto(),
    createdAt = createdAt,
    updatedAt = updatedAt,
)
