package com.peekr.common.jwt.domain.model.entity

data class JWTToken(
    val accessToken: String,
    val refreshToken: String,
)
