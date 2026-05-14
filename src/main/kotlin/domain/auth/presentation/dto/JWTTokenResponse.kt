package com.turnin.domain.auth.presentation.dto

import com.turnin.common.jwt.application.dto.JWTTokenDto
import kotlinx.serialization.Serializable

/** JWT 토큰 응답 바디 */
@Serializable
data class JWTTokenResponse(
    val accessToken: String,
    val refreshToken: String,
) {
    companion object {
        val sample = JWTTokenResponse(
            accessToken = "aaa.bbb.ccc",
            refreshToken = "aaa.bbb.ccc",
        )
    }
}

// ------------------------------ Mapper ------------------------------
fun JWTTokenDto.toResponse(): JWTTokenResponse = JWTTokenResponse(accessToken, refreshToken)
