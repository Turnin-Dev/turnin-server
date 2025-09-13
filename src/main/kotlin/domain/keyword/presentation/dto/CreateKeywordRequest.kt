package com.peekr.domain.keyword.presentation.dto

import kotlinx.serialization.Serializable

/**
 * 키워드 생성 요청 바디
 *
 * @property keyword 키워드명
 */
@Serializable
data class CreateKeywordRequest(val keyword: String) {
    companion object {
        val sample = CreateKeywordRequest("sample")
    }
}
