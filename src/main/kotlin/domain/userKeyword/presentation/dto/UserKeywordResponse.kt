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
 * @property createdAt 생성 일자
 * @property updatedAt 수정 일자
 */
@Serializable
data class UserKeywordResponse(
    val id: Long,
    val keywordId: Long,
    val keywordName: String,
    val userId: Long,
    val createdAt: Long,
    val updatedAt: Long,
) {
    companion object {
        val sample = UserKeywordResponse(
            id = 1,
            keywordId = 1,
            keywordName = "sample",
            userId = 1,
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
    createdAt = createdAt,
    updatedAt = updatedAt,
)
