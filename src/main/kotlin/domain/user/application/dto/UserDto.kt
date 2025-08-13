package com.peekr.domain.user.application.dto

import com.peekr.domain.user.domain.model.User

data class UserDto(
    val id: Long,
    val role: String,
    val provider: String,
    val providerId: String,
    val displayId: String,
    val name: String,
    val profileImageUrl: String?,
    val introduce: String?,
)

fun User.toDto(): UserDto = UserDto(
    id = id,
    role = role,
    provider = provider,
    providerId = providerId,
    displayId = displayId,
    name = name,
    profileImageUrl = profileImageUrl,
    introduce = introduce,
)
