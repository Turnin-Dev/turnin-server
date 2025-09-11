package com.peekr.domain.userKeyword.presentation.dto

import com.peekr.domain.core.model.KeywordId
import com.peekr.domain.core.model.UserId
import com.peekr.domain.userKeyword.application.dto.CreateUserKeywordDto
import kotlinx.serialization.Serializable

/**
 * 사용자별 키워드 생성 요청 바디
 *
 * @property userId 사용자 ID
 * @property keywordId 키워드 ID
 * @property offsetX UI 좌표 상에서의 X 위치
 * @property offsetY UI 좌표 상에서의 Y 위치
 * @property description 키워드 개인 설명
 */
@Serializable
data class CreateUserKeywordRequest(
    val userId: Long,
    val keywordId: Long,
    val offsetX: Float,
    val offsetY: Float,
    val description: String?,
)

fun CreateUserKeywordRequest.toDto(): CreateUserKeywordDto = CreateUserKeywordDto(
    userId = UserId(this.userId),
    keywordId = KeywordId(this.keywordId),
    offsetX = offsetX,
    offsetY = offsetY,
    description = description,
)
