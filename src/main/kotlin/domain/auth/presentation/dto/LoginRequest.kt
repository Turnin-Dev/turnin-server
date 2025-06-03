package com.peekr.domain.auth.presentation.dto

import com.peekr.domain.auth.domain.model.value.SocialLoginProvider
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val provider: SocialLoginProvider,
    val providerId: String,
    val name: String,
    val nickname: String,
    val profileImageUrl: String? = null,
    val introduce: String? = null,
)
