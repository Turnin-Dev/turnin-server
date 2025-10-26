package com.peekr.domain.userKeyword.presentation.dto

import com.peekr.domain.userKeyword.application.dto.UpdateDescriptionDto
import kotlinx.serialization.Serializable

/**
 * 사용자별 키워드 설명 수정 요청 바디
 *
 * @property description 키워드 개인 설명
 */
@Serializable
data class UpdateDescriptionRequest(val description: String?) {
    companion object {
        val sample = UpdateDescriptionRequest(
            description = "수정 샘플 키워드 설명",
        )
    }
}

fun UpdateDescriptionRequest.toDto(): UpdateDescriptionDto =
    UpdateDescriptionDto(description)
