package com.peekr.domain.model.entity.user

data class JwtToken(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long,
)
