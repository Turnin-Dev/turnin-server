package com.peekr.common.jwt.domain.model

data class JWTTokenPayload(
    val userId: String,
    val claimName: JWTClaimName,
    val claim: String,
)
