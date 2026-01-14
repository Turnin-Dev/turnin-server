package com.peekr.domain.userKeyword.presentation.dto

import com.peekr.domain.userKeyword.application.dto.UserInfoDto
import kotlinx.serialization.Serializable

/**
 * 사용자 정보 일부 응답 바디
 *
 * @property userId 사용자 ID
 * @property userName 사용자 명
 * @property profileImageUrl 프로필 사진 url
 */
@Serializable
data class UserInfoResponse(
    val userId: Long,
    val userName: String,
    val profileImageUrl: String?,
)

fun UserInfoDto.toResponse(): UserInfoResponse =
    UserInfoResponse(userId, userName, profileImageUrl)
