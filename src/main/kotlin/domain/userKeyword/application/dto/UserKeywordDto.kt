package com.turnin.domain.userKeyword.application.dto

import com.turnin.domain.userKeyword.domain.model.UserKeyword

/**
 * 사용자별 키워드 DTO
 *
 * @property id 사용자별 키워드 ID
 * @property keywordId 키워드 ID
 * @property keywordName 키워드 명
 * @property userId 사용자 ID
 * @property createdAt 생성 일자
 * @property updatedAt 수정 일자
 */
data class UserKeywordDto(
    val id: Long,
    val userId: Long,
    val keywordId: Long,
    val keywordName: String,
    val description: String?,
    val createdAt: Long,
    val updatedAt: Long,
)

fun UserKeyword.toDto(keywordName: String): UserKeywordDto = UserKeywordDto(
    id = id.value,
    userId = userId.value,
    keywordId = keywordId.value,
    keywordName = keywordName,
    description = description.value,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
