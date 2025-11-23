package com.peekr.domain.user.application.dto

import com.peekr.common.model.Introduce
import com.peekr.common.model.Name
import com.peekr.common.model.id.DisplayId
import com.peekr.domain.user.domain.model.UserProfile

/**
 * 사용자 프로필 DTO
 *
 * @property displayId 사용자 표시 ID
 * @property name 사용자 이름
 * @property profileImageUrl 사용자 프로필 이미지 url
 * @property introduce 사용자 소개 글
 * @property isActive 사용자 활성 여부
 * @property lastLoginAt 마지막 로그인 일시
 * @property friendsCount 사용자 친구 수
 */
data class UserProfileDto(
    val displayId: DisplayId,
    val name: Name,
    val profileImageUrl: String?,
    val introduce: Introduce?,
    val isActive: Boolean,
    val lastLoginAt: Long?,
    val friendsCount: Long,
)

fun UserProfile.toDto(): UserProfileDto =
    UserProfileDto(
        displayId = displayId,
        name = name,
        profileImageUrl = profileImageUrl,
        introduce = introduce,
        isActive = isActive,
        lastLoginAt = lastLoginAt,
        friendsCount = friendsCount,
    )
