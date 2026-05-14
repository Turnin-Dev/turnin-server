package com.turnin.domain.userKeyword.presentation.dto

import com.turnin.domain.userKeyword.application.dto.UserKeywordDto
import kotlinx.serialization.Serializable

/**
 * 사용자별 키워드 응답 바디
 *
 * @property id 사용자별 키워드 ID
 * @property keywordId 키워드 ID
 * @property keywordName 키워드 명
 * @property description 키워드 내용
 * @property createdAt 생성 일자
 * @property updatedAt 수정 일자
 */
@Serializable
data class UserKeywordResponse(
    val id: Long,
    val keywordId: Long,
    val keywordName: String,
    val description: String?,
    val createdAt: Long,
    val updatedAt: Long,
) {
    companion object {
        val sample = UserKeywordResponse(
            id = 1,
            keywordId = 1,
            keywordName = "sample",
            description = "description sample",
            createdAt = 1697875200L,
            updatedAt = 1697875200L,
        )
    }
}

fun UserKeywordDto.toResponse() = UserKeywordResponse(
    id = id,
    keywordId = keywordId,
    keywordName = keywordName,
    description = description,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
