package com.peekr.domain.keyword.presentation.dto

import com.peekr.domain.core.model.UserId
import com.peekr.domain.keyword.application.dto.AddUserKeywordDto
import kotlinx.serialization.Serializable

/**
 * 사용자별 키워드 추가 요청 바디
 *
 * @property userId 사용자 ID
 * @property keyword 키워드명
 * @property offsetX UI 좌표 상에서의 X 위치
 * @property offsetY UI 좌표 상에서의 Y 위치
 * @property description 키워드 개인 설명
 */
@Serializable
data class AddUserKeywordRequest(
    val userId: Long,
    val keyword: String,
    val offsetX: Float,
    val offsetY: Float,
    val description: String?,
)

fun AddUserKeywordRequest.toDto(): AddUserKeywordDto = AddUserKeywordDto(
    userId = UserId(this.userId),
    keyword = keyword,
    offsetX = offsetX,
    offsetY = offsetY,
    description = description,
)
