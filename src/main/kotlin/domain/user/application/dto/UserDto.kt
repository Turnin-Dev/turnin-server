package com.peekr.domain.user.application.dto

import com.peekr.domain.user.domain.model.User

data class UserDto(
    val id: Long,
    val provider: String,
    val providerId: String,
    val name: String,
    val nickname: String,
    val profileImageUrl: String?,
    val introduce: String?,
)

fun User.toDto(): UserDto = UserDto(
    id = id,
    provider = provider,
    providerId = providerId,
    name = name,
    nickname = nickname,
    profileImageUrl = profileImageUrl,
    introduce = introduce,
)
