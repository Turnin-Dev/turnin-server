package com.peekr.domain.userKeyword.presentation.dto

import com.peekr.domain.userKeyword.application.dto.UpdateDescriptionDto
import kotlinx.serialization.Serializable

/**
 * 사용자별 키워드 설명 수정 응답 바디
 *
 * @property description 키워드 개인 설명
 */
@Serializable
data class UpdateDescriptionResponse(val description: String?) {
    companion object {
        val sample = UpdateDescriptionResponse(
            description = "수정 샘플 키워드 설명",
        )
    }
}

fun UpdateDescriptionDto.toResponse() = UpdateDescriptionResponse(value)
