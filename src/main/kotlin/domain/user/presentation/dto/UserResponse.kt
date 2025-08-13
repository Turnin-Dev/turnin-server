package com.peekr.domain.user.presentation.dto

import com.peekr.domain.user.application.dto.UserDto
import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(
    val id: Long,
    val role: String,
    val provider: String,
    val providerId: String,
    val displayId: String,
    val name: String,
    val profileImageUrl: String?,
    val introduce: String?,
) {
    companion object {
        val sample = UserResponse(
            id = 1L,
            role = "USER",
            provider = "GOOGLE",
            providerId = "1231231231",
            displayId = "hong_gd_123",
            name = "honggd",
            profileImageUrl = "https://www.example.com/image.jpg",
            introduce = "hello world!",
        )
    }
}

fun UserDto.toResponse(): UserResponse = UserResponse(
    id = id,
    role = role,
    provider = provider,
    providerId = providerId,
    displayId = displayId,
    name = name,
    profileImageUrl = profileImageUrl,
    introduce = introduce,
)
