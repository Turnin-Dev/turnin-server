package com.peekr.common.jwt.domain.model.entity

data class JwtToken(
    val accessToken: String,
    val refreshToken: String,
)
