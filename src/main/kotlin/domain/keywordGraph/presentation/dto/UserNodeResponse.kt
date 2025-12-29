package com.peekr.domain.keywordGraph.presentation.dto

import com.peekr.domain.keywordGraph.application.dto.UserNodeDto
import kotlinx.serialization.Serializable

/**
 * 사용자 노드 모델 응답 바디
 *
 * @property userId 사용자 ID
 * @property userName 사용자 명
 * @property profileImageUrl 사용자 프로필 url
 */
@Serializable
data class UserNodeResponse(
    val userId: Long,
    val userName: String,
    val profileImageUrl: String?,
)

fun UserNodeDto.toResponse() =
    UserNodeResponse(
        userId = userId.value,
        userName = userName,
        profileImageUrl = profileImageUrl,
    )
