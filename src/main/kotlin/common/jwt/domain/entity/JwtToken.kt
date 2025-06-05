package com.peekr.common.jwt.domain.entity

data class JwtToken(
    val accessToken: String,
    val refreshToken: String,
)
