package com.peekr.domain.keyword.application.dto

import com.peekr.domain.core.model.KeywordId
import com.peekr.domain.core.model.UserId
import com.peekr.domain.keyword.domain.model.Keyword

/**
 * 키워드 DTO
 *
 * @property id 키워드 ID
 * @property keyword 키워드명
 * @property createdBy 키워드 최초등록자 ID
 * @property createdAt 키워드 등록 일자
 * @property updatedAt 키워드 수정 일자
 */
data class KeywordDto(
    val id: KeywordId,
    val keyword: String,
    val createdBy: UserId,
    val createdAt: Long,
    val updatedAt: Long,
)

fun Keyword.toDto(): KeywordDto = KeywordDto(
    id = this.id,
    keyword = this.keyword,
    createdBy = this.createdBy,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
)
