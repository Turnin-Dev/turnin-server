package com.turnin.domain.userKeyword.presentation.dto

import com.turnin.common.model.id.UserId
import com.turnin.domain.userKeyword.application.dto.CreateUserKeywordDto
import com.turnin.domain.userKeyword.application.dto.DescriptionDto
import kotlinx.serialization.Serializable

/**
 * 사용자별 키워드 생성 요청 바디
 *
 * @property userId 사용자 ID
 * @property keywordName 키워드 명
 * @property description 키워드 개인 설명
 */
@Serializable
data class CreateUserKeywordRequest(
    val userId: Long,
    val keywordName: String,
    val description: String?,
) {
    companion object {
        val sample = CreateUserKeywordRequest(
            userId = 1,
            keywordName = "sample",
            description = "샘플 키워드",
        )
    }
}

fun CreateUserKeywordRequest.toDto(): CreateUserKeywordDto = CreateUserKeywordDto(
    userId = UserId(this.userId),
    keywordName = keywordName,
    description = DescriptionDto(description),
)
