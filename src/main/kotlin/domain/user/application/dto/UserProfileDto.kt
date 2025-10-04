package com.peekr.domain.user.application.dto

import com.peekr.domain.user.domain.model.UserProfile

/** 애플리케이션 계층에서 사용하는 UserProfile */
data class UserProfileDto(
    val user: UserDto,
    val friendsCount: Long,
)

fun UserProfile.toDto(): UserProfileDto =
    UserProfileDto(
        user = user.toDto(),
        friendsCount = friendsCount,
    )
