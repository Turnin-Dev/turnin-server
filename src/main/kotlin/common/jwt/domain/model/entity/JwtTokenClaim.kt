package com.peekr.common.jwt.domain.model.entity

import com.peekr.common.jwt.domain.model.value.JwtClaimName

data class JwtTokenPayload(
    val subject: String,
    val claimName: JwtClaimName,
    val claim: String,
)
