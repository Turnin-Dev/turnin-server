package com.peekr.domain.user.presentation.dto

import com.peekr.domain.user.application.dto.UserProfileDto
import kotlinx.serialization.Serializable

/**
 * 사용자 프로필 조회 응답 바디
 *
 * @property id 사용자 ID
 * @property role 사용자 역할
 * @property provider 소셜로그인 플랫폼
 * @property providerId 소셜로그인 ID
 * @property displayId 사용자 표시 ID
 * @property name 사용자 이름
 * @property profileImageUrl 사용자 프로필 이미지 url
 * @property introduce 사용자 소개 글
 * @property isActive 사용자 활성 여부
 * @property lastLoginAt 마지막 로그인 일시
 * @property friendsCount 친구 수
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
    introduce = user.introduce?.value,
    isActive = user.isActive,
    lastLoginAt = user.lastLoginAt,
    friendsCount = friendsCount,
)
