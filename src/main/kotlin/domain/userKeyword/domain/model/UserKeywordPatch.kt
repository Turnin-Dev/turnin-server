package com.peekr.domain.userKeyword.domain.model

/**
 * 업데이트 용 사용자별 키워드
 *
 * @property offsetX UI 좌표 상에서의 X 위치
 * @property offsetY UI 좌표 상에서의 Y 위치
 * @property description 키워드 개인 설명
 */
data class UserKeywordPatch(
    val offsetX: Float,
    val offsetY: Float,
    val description: String?,
)
