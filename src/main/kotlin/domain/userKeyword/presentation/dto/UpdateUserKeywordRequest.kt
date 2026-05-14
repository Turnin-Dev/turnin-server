package com.turnin.domain.userKeyword.presentation.dto

import com.turnin.domain.userKeyword.application.dto.UserKeywordPatchDto
import kotlinx.serialization.Serializable

/**
 * 사용자 키워드 수정 요청 바디
 *
 * @property userKeywordId 사용자 키워드 ID
 * @property keywordName 키워드 명
 * @property description 키워드 내용
 */
@Serializable
data class UpdateUserKeywordRequest(
    val userKeywordId: Long,
    val keywordName: String,
    val description: String,
) {
    companion object {
        val sample = UpdateUserKeywordRequest(
            userKeywordId = 1L,
            keywordName = "새로운 키워드",
            description = "새로운 키워드 설명",
        )
    }
}

fun UpdateUserKeywordRequest.toDto() = UserKeywordPatchDto(
    userKeywordId = userKeywordId,
    keywordName = keywordName,
    description = description,
)
