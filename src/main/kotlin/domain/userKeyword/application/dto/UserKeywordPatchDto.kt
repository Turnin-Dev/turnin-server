package com.turnin.domain.userKeyword.application.dto

import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.userKeyword.domain.model.Description
import com.turnin.domain.userKeyword.domain.model.UserKeywordPatch

/**
 * 사용자 키워드 수정 DTO
 *
 * @property userKeywordId 사용자 키워드 ID
 * @property keywordName 키워드 명
 * @property description 키워드 내용 DTO
 */
data class UserKeywordPatchDto(
    val userKeywordId: Long,
    val keywordName: String,
    val description: String,
)

fun UserKeywordPatchDto.toDomain(keywordId: KeywordId): UserKeywordPatch = UserKeywordPatch(
    userKeywordId = UserKeywordId(userKeywordId),
    keywordId = keywordId,
    description = Description(description),
)
