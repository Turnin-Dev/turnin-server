package com.peekr.domain.userKeyword.domain.model

import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId

/**
 * 사용자별 키워드
 *
 * @property id 사용자별 키워드 ID
 * @property keywordId 키워드 ID
 * @property userId 사용자 ID
 * @property createdAt 생성 일자
 * @property updatedAt 수정 일자
 */
data class UserKeyword(
    val id: UserKeywordId,
    val keywordId: KeywordId,
    val userId: UserId,
    val createdAt: Long,
    val updatedAt: Long,
)
