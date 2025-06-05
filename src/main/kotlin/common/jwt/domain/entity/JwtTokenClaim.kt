package com.peekr.common.jwt.domain.entity

data class JwtTokenPayload(
    val subject: String,
    val claimName: String,
    val claim: String,
)
