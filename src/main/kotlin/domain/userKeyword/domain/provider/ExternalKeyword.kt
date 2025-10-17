package com.peekr.domain.userKeyword.domain.provider

import com.peekr.common.model.KeywordId
import com.peekr.common.model.UserId

/**
 * 외부에서 제공된 키워드
 *
 * @property id 키워드 ID
 * @property keyword 키워드명
 * @property createdBy 키워드 최초등록자 ID
 * @property createdAt 키워드 등록 일자
 * @property updatedAt 키워드 수정 일자
 */
data class ExternalKeyword(
    val id: KeywordId,
    val keyword: String,
    val createdBy: UserId,
    val createdAt: Long,
    val updatedAt: Long,
)
