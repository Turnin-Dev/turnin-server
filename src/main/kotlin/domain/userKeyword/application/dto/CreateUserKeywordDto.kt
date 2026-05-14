package com.turnin.domain.userKeyword.application.dto

import com.turnin.common.model.id.UserId

/**
 * 사용자별 키워드 DTO
 *
 * @property userId 사용자 ID
 * @property keywordName 키워드 명
 * @property description 키워드 개인 설명
 */
data class CreateUserKeywordDto(
    val userId: UserId,
    val keywordName: String,
    val description: DescriptionDto,
)
