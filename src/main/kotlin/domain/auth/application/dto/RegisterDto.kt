package com.peekr.domain.auth.application.dto

import com.peekr.domain.auth.domain.model.RoleForAuth
import com.peekr.domain.auth.domain.model.SocialLoginProviderForAuth

data class RegisterDto(
    val role: RoleForAuth,
    val provider: SocialLoginProviderForAuth,
    val providerId: String,
    val displayId: String,
    val name: String,
    val profileImageUrl: String? = null,
    val introduce: String? = null,
)
