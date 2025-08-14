package com.peekr.domain.auth.application.dto

import com.peekr.domain.auth.domain.model.SocialLoginProviderForAuth

data class LoginDto(
    val provider: SocialLoginProviderForAuth,
    val providerId: String,
)
