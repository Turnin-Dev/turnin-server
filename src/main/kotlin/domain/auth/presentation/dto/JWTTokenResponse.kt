package com.peekr.domain.auth.presentation.dto

import kotlinx.serialization.Serializable

@Serializable
data class JWTTokenResponse(
    val accessToken: String,
    val refreshToken: String,
)
