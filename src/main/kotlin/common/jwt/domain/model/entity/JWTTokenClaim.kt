package com.peekr.common.jwt.domain.model.entity

import com.peekr.common.jwt.domain.model.value.JWTClaimName

data class JWTTokenPayload(
    val subject: String,
    val claimName: JWTClaimName,
    val claim: String,
)
