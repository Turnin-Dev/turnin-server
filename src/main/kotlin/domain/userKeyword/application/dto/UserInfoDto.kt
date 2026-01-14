package com.peekr.domain.userKeyword.application.dto

import com.peekr.domain.userKeyword.domain.model.UserInfo

/**
 * 사용자 정보 일부 DTO
 *
 * @property userId 사용자 ID
 * @property userName 사용자 명
 * @property profileImageUrl 프로필 사진 url
 */
data class UserInfoDto(
    val userId: Long,
    val userName: String,
    val profileImageUrl: String?,
)

fun UserInfo.toDto(): UserInfoDto =
    UserInfoDto(userId.value, userName.value, profileImageUrl)
