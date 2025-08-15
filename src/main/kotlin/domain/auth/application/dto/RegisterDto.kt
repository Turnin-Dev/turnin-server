package com.peekr.domain.auth.application.dto

import com.peekr.domain.auth.domain.model.RoleForAuth
import com.peekr.domain.auth.domain.model.SocialLoginProviderForAuth
import java.time.Instant

/** 애플리케이션 계층에서 사용하는 RegisterDto */
data class RegisterDto(
    val role: RoleForAuth,
    val provider: SocialLoginProviderForAuth,
    val providerId: String,
    val displayId: String,
    val name: String,
    val profileImageUrl: String?,
    val introduce: String?,
    val isActive: Boolean,
    val lastLoginAt: Instant?,
)
