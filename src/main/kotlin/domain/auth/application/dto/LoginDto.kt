package com.peekr.domain.auth.application.dto

import com.peekr.common.db.scheme.SocialLoginProvider

data class LoginDto(
    val provider: SocialLoginProvider,
    val providerId: String,
)
