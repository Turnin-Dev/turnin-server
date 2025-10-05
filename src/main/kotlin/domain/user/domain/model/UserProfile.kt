package com.peekr.domain.user.domain.model

/**
 * 사용자 프로필
 *
 * @property user [User]
 * @property friendsCount 사용자 친구 수
 */
data class UserProfile(
    val user: User,
    val friendsCount: Long,
)
