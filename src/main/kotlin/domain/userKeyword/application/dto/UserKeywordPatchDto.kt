package com.peekr.domain.userKeyword.application.dto

import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.userKeyword.domain.model.Description
import com.peekr.domain.userKeyword.domain.model.UserKeywordPatch

/**
 * 사용자 키워드 수정 DTO
 *
 * @property ownerId 사용자 ID
 * @property userKeywordId 사용자 키워드 ID
 * @property keywordName 키워드 명
 * @property description 키워드 내용 DTO
 */
data class UserKeywordPatchDto(
    val ownerId: Long,
    val userKeywordId: Long,
    val keywordName: String,
    val description: String,
)

fun UserKeywordPatchDto.toDomain(keywordId: KeywordId): UserKeywordPatch = UserKeywordPatch(
    ownerId = UserId(ownerId),
    userKeywordId = UserKeywordId(userKeywordId),
    keywordId = keywordId,
    description = Description(description),
)
