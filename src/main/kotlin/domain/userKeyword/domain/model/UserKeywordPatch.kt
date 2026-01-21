package com.peekr.domain.userKeyword.domain.model

import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId

/**
 * 사용자 키워드 수정 모델
 *
 * @property ownerId 사용자 ID
 * @property userKeywordId 사용자 키워드 ID
 * @property keywordId 키워드 ID
 * @property description 키워드 내용
 */
data class UserKeywordPatch(
    val ownerId: UserId,
    val userKeywordId: UserKeywordId,
    val keywordId: KeywordId,
    val description: Description,
)
