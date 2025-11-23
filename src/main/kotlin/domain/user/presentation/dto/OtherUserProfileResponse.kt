package com.peekr.domain.user.presentation.dto

import com.peekr.common.model.FriendshipStatus
import com.peekr.domain.user.application.dto.OtherUserProfileDto
import kotlinx.serialization.Serializable

/**
 * 다른 사용자 프로필 조회 응답 바디
 *
 * @property displayId 사용자 표시 ID
 * @property name 사용자 이름
 * @property profileImageUrl 사용자 프로필 이미지 url
 * @property introduce 사용자 소개 글
 * @property isActive 사용자 활성 여부
 * @property lastLoginAt 마지막 로그인 일시
 * @property friendsCount 친구 수
 * @property friendshipStatus 친구 관계 상태
 */
@Serializable
data class OtherUserProfileResponse(
    val displayId: String,
    val name: String,
    val profileImageUrl: String?,
    val introduce: String?,
    val isActive: Boolean,
    val lastLoginAt: Long?,
    val friendsCount: Long,
    val friendshipStatus: FriendshipStatus,
) {
    companion object {
        val sample = OtherUserProfileResponse(
            displayId = "hong_gd_123",
            name = "honggd",
            profileImageUrl = "https://www.example.com/image.jpg",
            introduce = "hello world!",
            isActive = true,
            lastLoginAt = 1697875200000L,
            friendsCount = 51L,
            friendshipStatus = FriendshipStatus.NOTHING,
        )
    }
}

fun OtherUserProfileDto.toResponse(): OtherUserProfileResponse = OtherUserProfileResponse(
    displayId = userProfileDto.displayId.value,
    name = userProfileDto.name.value,
    profileImageUrl = userProfileDto.profileImageUrl,
    introduce = userProfileDto.introduce?.value,
    isActive = userProfileDto.isActive,
    lastLoginAt = userProfileDto.lastLoginAt,
    friendsCount = userProfileDto.friendsCount,
    friendshipStatus = friendshipStatus,
)
