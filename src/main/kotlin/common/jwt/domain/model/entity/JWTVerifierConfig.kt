package com.peekr.common.jwt.domain.model.entity

data class JWTVerifierConfig(
    val secretKey: String,
    val audience: String,
    val issuer: String,
)
