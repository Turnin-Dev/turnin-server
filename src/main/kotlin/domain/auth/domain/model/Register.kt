package com.peekr.domain.auth.domain.model

import com.peekr.common.model.Introduce
import com.peekr.common.model.Name
import com.peekr.common.model.Role
import com.peekr.common.model.SocialLoginProvider
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.UserId
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
    val provider: SocialLoginProvider,
    val providerId: String,
    val displayId: DisplayId,
    val name: Name,
    val profileImageUrl: String?,
    val introduce: Introduce?,
)

fun Register.toAuthUser(
    id: Long,
    role: Role,
    isActive: Boolean,
    lastLoginAt: Instant?,
): AuthUser = AuthUser(
    userId = UserId(id),
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
