package com.peekr.domain.userKeyword.presentation.dto

import com.peekr.domain.userKeyword.application.dto.UserKeywordPatchDto
import kotlinx.serialization.Serializable

/**
 * 사용자 키워드 수정 요청 바디
 *
 * @property ownerId 사용자 ID
 * @property userKeywordId 사용자 키워드 ID
 * @property keywordName 키워드 명
 * @property description 키워드 내용
 */
@Serializable
data class UpdateUserKeywordRequest(
    val ownerId: Long,
    val userKeywordId: Long,
    val keywordName: String,
    val description: String,
) {
    companion object {
        val sample = UpdateUserKeywordRequest(
            ownerId = 1L,
            userKeywordId = 1L,
            keywordName = "새로운 키워드",
            description = "새로운 키워드 설명",
        )
    }
}

fun UpdateUserKeywordRequest.toDto() = UserKeywordPatchDto(
    ownerId = ownerId,
    userKeywordId = userKeywordId,
    keywordName = keywordName,
    description = description,
)
