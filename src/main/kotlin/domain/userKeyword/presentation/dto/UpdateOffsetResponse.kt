package com.peekr.domain.userKeyword.presentation.dto

import com.peekr.domain.userKeyword.application.dto.OffsetDto
import kotlinx.serialization.Serializable

/**
 * 사용자별 키워드 오프셋 수정 응답 바디
 *
 * @property offsetX UI 좌표 상에서의 X 위치
 * @property offsetY UI 좌표 상에서의 Y 위치
 */
@Serializable
data class UpdateOffsetResponse(
    val offsetX: Float,
    val offsetY: Float,
) {
    companion object {
        val sample = UpdateOffsetResponse(
            offsetX = 0f,
            offsetY = 0f,
        )
    }
}

fun OffsetDto.toResponse() = UpdateOffsetResponse(x, y)
