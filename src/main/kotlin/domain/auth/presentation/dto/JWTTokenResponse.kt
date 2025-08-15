package com.peekr.domain.auth.presentation.dto

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
