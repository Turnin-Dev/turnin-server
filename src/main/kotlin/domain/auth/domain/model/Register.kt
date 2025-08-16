package com.peekr.domain.auth.domain.model

import java.time.Instant

/**
 * 회원가입 정보
 *
 * @property provider 소셜로그인 제공자
 * @property providerId 소셜로그인 제공자에서 제공한 ID
 * @property displayId 사용자 표시 ID
 * @property name 사용자 이름
 * @property profileImageUrl 사용자 프로필 사진 url
 * @property introduce 사용자 소개 글
 */
data class Register(
    val provider: SocialLoginProviderForAuth,
    val providerId: String,
    val displayId: String,
    val name: String,
    val profileImageUrl: String?,
    val introduce: String?,
)

fun Register.toAuthUser(
    id: Long,
    role: RoleForAuth,
    isActive: Boolean,
    lastLoginAt: Instant?,
): AuthUser = AuthUser(
    id = id,
    role = role,
    provider = provider,
    providerId = providerId,
    name = name,
    displayId = displayId,
    profileImageUrl = profileImageUrl,
    introduce = introduce,
    isActive = isActive,
    lastLoginAt = lastLoginAt,
)
