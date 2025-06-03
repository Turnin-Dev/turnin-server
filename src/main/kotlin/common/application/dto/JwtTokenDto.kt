package com.peekr.common.application.dto

data class JwtTokenDto(
    val accessToken: String,
    val refreshToken: String,
)
