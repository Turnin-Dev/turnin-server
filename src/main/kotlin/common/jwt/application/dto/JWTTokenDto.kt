package com.peekr.common.jwt.application.dto

import com.peekr.common.jwt.domain.model.JWTToken

data class JWTTokenDto(
    val accessToken: String,
    val refreshToken: String,
)

fun JWTToken.toDto(): JWTTokenDto = JWTTokenDto(accessToken, refreshToken)
