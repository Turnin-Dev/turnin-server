package com.peekr.domain.keyword.application.dto

import com.peekr.common.model.KeywordId
import com.peekr.common.model.UserId
import com.peekr.domain.keyword.domain.model.Keyword

/**
 * 키워드 DTO
 *
 * @property id 키워드 ID
 * @property name 키워드명
 * @property createdBy 키워드 최초등록자 ID
 * @property createdAt 키워드 등록 일자
 * @property updatedAt 키워드 수정 일자
 */
data class KeywordDto(
    val id: KeywordId,
    val name: String,
    val createdBy: UserId,
    val createdAt: Long,
    val updatedAt: Long,
)

fun Keyword.toDto(): KeywordDto = KeywordDto(
    id = this.id,
    name = this.name.value,
    createdBy = this.createdBy,
    createdAt = this.createdAt,
    updatedAt = this.updatedAt,
)
