package com.peekr.domain.auth.application.dto

import com.peekr.domain.auth.domain.model.value.SocialLoginProvider

data class LoginDto(
    val provider: SocialLoginProvider,
    val providerId: String,
    val name: String?,
    val nickname: String?,
    val profileImageUrl: String?,
    val introduce: String?,
)
