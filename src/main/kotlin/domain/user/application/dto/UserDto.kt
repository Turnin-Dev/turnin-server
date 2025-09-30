package com.peekr.domain.user.application.dto

import com.peekr.common.model.DisplayId
import com.peekr.common.model.Name
import com.peekr.common.model.UserId
import com.peekr.domain.user.domain.model.RoleForUser
import com.peekr.domain.user.domain.model.SocialLoginProviderForUser
import com.peekr.domain.user.domain.model.User

/** 애플리케이션 계층에서 사용하는 User */
data class UserDto(
    val id: UserId,
    val role: RoleForUser,
    val provider: SocialLoginProviderForUser,
    val providerId: String,
    val displayId: DisplayId,
    val name: Name,
    val profileImageUrl: String?,
    val introduce: String?,
    val isActive: Boolean,
    val lastLoginAt: Long?,
)

fun User.toDto(): UserDto = UserDto(
    id = id,
    role = role,
    provider = provider,
    providerId = providerId,
    displayId = displayId,
    name = name,
    profileImageUrl = profileImageUrl,
    introduce = introduce,
    isActive = isActive,
    lastLoginAt = lastLoginAt?.toEpochMilli(),
)
