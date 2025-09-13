package com.peekr.domain.userKeyword.presentation.dto

import com.peekr.domain.userKeyword.application.dto.UserKeywordPatchDto
import kotlinx.serialization.Serializable

/**
 * 업데이트 용 사용자별 키워드 요청 바디
 *
 * @property offsetX UI 좌표 상에서의 X 위치
 * @property offsetY UI 좌표 상에서의 Y 위치
 * @property description 키워드 개인 설명
 */
@Serializable
data class PatchUserKeywordRequest(
    val offsetX: Float,
    val offsetY: Float,
    val description: String?,
) {
    companion object {
        val sample = PatchUserKeywordRequest(
            offsetX = 0f,
            offsetY = 0f,
            description = "수정 샘플 키워드 설명",
        )
    }
}

fun PatchUserKeywordRequest.toDto(): UserKeywordPatchDto =
    UserKeywordPatchDto(offsetX, offsetY, description)
