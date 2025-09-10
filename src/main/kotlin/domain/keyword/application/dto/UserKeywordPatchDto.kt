package com.peekr.domain.keyword.application.dto

import com.peekr.domain.keyword.domain.model.UserKeywordPatch

/**
 * 업데이트 용 사용자별 키워드 DTO
 *
 * @property offsetX UI 좌표 상에서의 X 위치
 * @property offsetY UI 좌표 상에서의 Y 위치
 * @property description 키워드 개인 설명
 */
data class UserKeywordPatchDto(
    val offsetX: Float,
    val offsetY: Float,
    val description: String?,
)

fun UserKeywordPatchDto.toDomain() = UserKeywordPatch(offsetX, offsetY, description)
