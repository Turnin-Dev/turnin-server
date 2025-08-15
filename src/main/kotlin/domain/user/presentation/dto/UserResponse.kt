package com.peekr.domain.user.presentation.dto

import com.peekr.domain.user.application.dto.UserDto
import kotlinx.serialization.Serializable

/**
 * 사용자 조회 응답 바디
 */
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
    val isActive: Boolean,
    val lastLoginAt: Long?,
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
            isActive = true,
            lastLoginAt = 1697875200000,
        )
    }
}

fun UserDto.toResponse(): UserResponse = UserResponse(
    id = id,
    role = role.name,
    provider = provider.name,
    providerId = providerId,
    displayId = displayId,
    name = name,
    profileImageUrl = profileImageUrl,
    introduce = introduce,
    isActive = isActive,
    lastLoginAt = lastLoginAt,
)
