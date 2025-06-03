package com.peekr.presentation.dto.auth

import com.peekr.domain.model.value.auth.SocialLoginProvider
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
