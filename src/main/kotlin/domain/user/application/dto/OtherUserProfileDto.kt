package com.peekr.domain.user.application.dto

import com.peekr.domain.user.domain.provider.ExternalFriendshipStatus

/**
 * 다른 사용자 프로필 DTO
 *
 * @property userProfileDto 사용자 프로필 DTO
 * @property friendshipStatus 친구 관계 상태
 */
data class OtherUserProfileDto(
    val userProfileDto: UserProfileDto,
    val friendshipStatus: ExternalFriendshipStatus,
)
