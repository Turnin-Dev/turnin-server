package com.peekr.domain.userKeyword.presentation.dto

import com.peekr.common.model.UserId
import com.peekr.domain.userKeyword.application.dto.CreateUserKeywordDto
import kotlinx.serialization.Serializable

/**
 * 사용자별 키워드 생성 요청 바디
 *
 * @property userId 사용자 ID
 * @property keywordName 키워드 명
 * @property offsetX UI 좌표 상에서의 X 위치
 * @property offsetY UI 좌표 상에서의 Y 위치
 * @property description 키워드 개인 설명
 */
@Serializable
data class CreateUserKeywordRequest(
    val userId: Long,
    val keywordName: String,
    val offsetX: Float,
    val offsetY: Float,
    val description: String?,
) {
    companion object {
        val sample = CreateUserKeywordRequest(
            userId = 1,
            keywordName = "sample",
            offsetX = 0f,
            offsetY = 0f,
            description = "샘플 키워드",
        )
    }
}

fun CreateUserKeywordRequest.toDto(): CreateUserKeywordDto = CreateUserKeywordDto(
    userId = UserId(this.userId),
    keywordName = keywordName,
    offsetX = offsetX,
    offsetY = offsetY,
    description = description,
)
