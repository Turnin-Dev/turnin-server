package com.peekr.domain.userKeyword.presentation.dto

import com.peekr.domain.userKeyword.application.dto.DescriptionDto
import kotlinx.serialization.Serializable

/**
 * 사용자별 키워드 설명 응답 바디
 *
 * @property description 키워드 개인 설명
 */
@Serializable
data class DescriptionResponse(val description: String?) {
    companion object {
        val sample = DescriptionResponse(
            description = "샘플 키워드 설명",
        )
    }
}

fun DescriptionDto.toResponse() = DescriptionResponse(value)
