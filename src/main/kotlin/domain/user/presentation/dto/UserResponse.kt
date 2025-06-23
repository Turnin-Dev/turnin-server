package com.peekr.domain.user.presentation.dto

import com.peekr.domain.user.application.dto.UserDto
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: Long,
    val provider: String,
    val providerId: String,
    val name: String,
    val nickname: String,
    val profileImageUrl: String?,
    val introduce: String?,
) {
    companion object {
        val sample = UserResponse(
            id = 1L,
            provider = "Google",
            providerId = "1231231231",
            name = "honggd",
            nickname = "hongddddddd",
            profileImageUrl = "https://www.example.com/image.jpg",
            introduce = "hello world!",
        )
    }
}

fun UserDto.toResponse(): UserResponse = UserResponse(
    id = id,
    provider = provider,
    providerId = providerId,
    name = name,
    nickname = nickname,
    profileImageUrl = profileImageUrl,
    introduce = introduce,
)
