package com.peekr.domain.userKeyword.domain.provider

import com.peekr.common.model.KeywordName
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId

/**
 * 외부에서 제공된 키워드
 *
 * @property id 키워드 ID
 * @property name 키워드명
 * @property createdBy 키워드 최초등록자 ID (탈퇴/비식별화된 경우 null)
 * @property createdAt 키워드 등록 일자
 * @property updatedAt 키워드 수정 일자
 */
data class ExternalKeyword(
    val id: KeywordId,
    val name: KeywordName,
    val createdBy: UserId?,
    val createdAt: Long,
    val updatedAt: Long,
)
