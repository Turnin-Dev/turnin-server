package com.peekr.domain.keyword.presentation.dto

import com.peekr.domain.keyword.application.dto.KeywordDto
import kotlinx.serialization.Serializable

/**
 * 키워드 응답바디
 *
 * @property id 키워드 ID
 * @property keyword 키워드명
 * @property createdBy 키워드 최초등록자 ID
 * @property createdAt 키워드 등록 일자
 * @property updatedAt 키워드 수정 일자
 */
@Serializable
data class KeywordResponse(
    val id: Long,
    val keyword: String,
    val createdBy: Long,
    val createdAt: Long,
    val updatedAt: Long,
) {
    companion object {
        val sample = KeywordResponse(
            id = 1,
            keyword = "sample",
            createdBy = 1,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
    }
}

fun KeywordDto.toResponse(): KeywordResponse = KeywordResponse(
    id = id.value,
    keyword = keyword,
    createdBy = createdBy.value,
    createdAt = createdAt,
    updatedAt = updatedAt,
)
