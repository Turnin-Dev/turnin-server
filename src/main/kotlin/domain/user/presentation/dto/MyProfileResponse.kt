package com.peekr.domain.user.presentation.dto

import com.peekr.domain.user.application.dto.MyProfileDto
import kotlinx.serialization.Serializable

/**
 * 나의 프로필 조회 응답 바디
 *
 * @property userId 사용자 ID
 * @property displayId 사용자 표시 ID
 * @property name 사용자 이름
 * @property profileImageUrl 사용자 프로필 이미지 url
 * @property introduce 사용자 소개 글
 * @property lastLoginAt 마지막 로그인 일시
 * @property friendsCount 친구 수
 * @property isActive 사용자 활성 여부
 */
@Serializable
data class MyProfileResponse(
    val userId: Long,
    val displayId: String,
    val name: String,
    val profileImageUrl: String?,
    val introduce: String?,
    val lastLoginAt: Long?,
    val friendsCount: Long,
    val isActive: Boolean,
) {
    companion object {
        val sample = MyProfileResponse(
            userId = 1L,
            displayId = "hong_gd_123",
            name = "honggd",
            profileImageUrl = "https://www.example.com/image.jpg",
            introduce = "hello world!",
            lastLoginAt = 1697875200000L,
            friendsCount = 51L,
            isActive = true,
        )
    }
}

fun MyProfileDto.toResponse(): MyProfileResponse = MyProfileResponse(
    userId = userId,
    displayId = displayId.value,
    name = name.value,
    profileImageUrl = profileImageUrl,
    introduce = introduce?.value,
    isActive = isActive,
    lastLoginAt = lastLoginAt,
    friendsCount = friendsCount,
)
