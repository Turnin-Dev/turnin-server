package com.peekr.common.domain.entity

data class JwtToken(
    val accessToken: String,
    val refreshToken: String,
)
