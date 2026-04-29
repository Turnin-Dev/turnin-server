package com.turnin.common.jwt.application.dto

import com.turnin.common.jwt.domain.model.JWTToken

data class JWTTokenDto(
    val accessToken: String,
    val refreshToken: String,
)

fun JWTToken.toDto(): JWTTokenDto = JWTTokenDto(accessToken, refreshToken)
