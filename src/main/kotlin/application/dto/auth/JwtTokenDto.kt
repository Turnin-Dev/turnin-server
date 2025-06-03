package com.peekr.application.dto.auth

data class JwtTokenDto(
    val accessToken: String,
    val refreshToken: String,
)
