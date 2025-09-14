package com.peekr.domain.user.domain.model

import com.peekr.domain.core.model.UserId
import java.time.Instant

/**
 * User
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
data class User(
    val id: UserId,
    val role: RoleForUser,
    val provider: SocialLoginProviderForUser,
    val providerId: String,
    val displayId: String,
    val name: String,
    val profileImageUrl: String?,
    val introduce: String?,
    val isActive: Boolean,
    val lastLoginAt: Instant?,
)
