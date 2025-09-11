package com.peekr.domain.keyword.presentation.dto

import kotlinx.serialization.Serializable

/**
 * 키워드 생성 요청 바디
 *
 * @property keyword 키워드명
 * @property createdBy 키워드 최초등록자 ID
 */
@Serializable
data class CreateKeywordRequest(
    val keyword: String,
    val createdBy: Long,
)
