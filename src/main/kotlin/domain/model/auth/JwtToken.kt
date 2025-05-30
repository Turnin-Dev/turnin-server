package com.peekr.domain.model.auth

/** JWT 토큰 */
data class JwtToken(
    val accessToken: String,
    val refreshToken: String,
)
