package com.peekr.domain.user.presentation.dto

import com.peekr.domain.user.application.dto.UserProfileDto
import kotlinx.serialization.Serializable

/**
 * 사용자 프로필 조회 응답 바디
 */
@Serializable
data class UserProfileResponse(
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
    val friendsCount: Long,
) {
    companion object {
        val sample = UserProfileResponse(
            id = 1L,
            role = "USER",
            provider = "GOOGLE",
            providerId = "1231231231",
            displayId = "hong_gd_123",
            name = "honggd",
            profileImageUrl = "https://www.example.com/image.jpg",
            introduce = "hello world!",
            isActive = true,
            lastLoginAt = 1697875200000L,
            friendsCount = 51L,
        )
    }
}

fun UserProfileDto.toResponse(): UserProfileResponse = UserProfileResponse(
    id = user.id.value,
    role = user.role.name,
    provider = user.provider.name,
    providerId = user.providerId,
    displayId = user.displayId.value,
    name = user.name.value,
    profileImageUrl = user.profileImageUrl,
    introduce = user.introduce,
    isActive = user.isActive,
    lastLoginAt = user.lastLoginAt,
    friendsCount = friendsCount,
)
