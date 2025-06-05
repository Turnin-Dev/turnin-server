package com.peekr.common.jwt.application.dto

data class JwtTokenDto(
    val accessToken: String,
    val refreshToken: String,
)
