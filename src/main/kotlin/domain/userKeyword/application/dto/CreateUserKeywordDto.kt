package com.peekr.domain.userKeyword.application.dto

import com.peekr.common.model.UserId

/**
 * 사용자별 키워드 DTO
 *
 * @property userId 사용자 ID
 * @property keywordName 키워드 명
 * @property offsetX UI 좌표 상에서의 X 위치
 * @property offsetY UI 좌표 상에서의 Y 위치
 * @property description 키워드 개인 설명
 */
data class CreateUserKeywordDto(
    val userId: UserId,
    val keywordName: String,
    val offsetX: Float,
    val offsetY: Float,
    val description: String?,
)
