package com.peekr.domain.auth.application.dto

import com.peekr.domain.auth.domain.model.value.SocialLoginProvider

data class LoginDto(
    val provider: SocialLoginProvider,
    val providerId: String,
)
