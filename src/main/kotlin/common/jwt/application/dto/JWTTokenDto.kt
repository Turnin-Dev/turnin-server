package com.peekr.common.jwt.application.dto

data class JWTTokenDto(
    val accessToken: String,
    val refreshToken: String,
)
