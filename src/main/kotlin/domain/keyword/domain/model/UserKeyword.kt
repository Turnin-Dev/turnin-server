package com.peekr.domain.keyword.domain.model

import com.peekr.domain.common.model.UserId

/**
 * 사용자별 키워드
 *
 * @property id 사용자별 키워드 ID
 * @property keywordId 키워드 ID
 * @property userId 사용자 ID
 * @property offsetX UI 좌표 상에서의 X 위치
 * @property offsetY UI 좌표 상에서의 Y 위치
 * @property description 키워드 개인 설명
 * @property createdAt 생성 일자
 * @property updatedAt 수정 일자
 */
data class UserKeyword(
    val id: UserKeywordId,
    val keywordId: KeywordId,
    val userId: UserId,
    val offsetX: Float,
    val offsetY: Float,
    val description: String?,
    val createdAt: Long,
    val updatedAt: Long,
)
