package com.peekr.domain.user.application.dto

import com.peekr.common.model.Introduce
import com.peekr.common.model.Name
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.UserId
import com.peekr.domain.user.domain.model.User

/**
 * User DTO
 *
 * @param id 사용자 ID
 * @param role 사용자 역할
 * @param provider 소셜로그인 플랫폼
 * @param providerId 소셜로그인 ID
 * @param displayId 사용자 표시 ID
 * @param name 사용자 이름
 * @param profileImageUrl 사용자 프로필 이미지 url
 * @param introduce 사용자 소개 글
 * @param isActive 사용자 활성 여부
 * @param lastLoginAt 마지막 로그인 일시
 */
data class UserDto(
    val id: UserId,
    val role: Role,
    val provider: SocialLoginProvider,
    val providerId: String,
    val displayId: DisplayId,
    val name: Name,
    val profileImageUrl: String?,
    val introduce: Introduce?,
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
