package com.peekr.common.jwt.domain.model

enum class JWTTokenType {
    Access,
    Refresh,
}

data class JWTToken(
    val accessToken: String,
    val refreshToken: String,
) {
    companion object {
        fun String.removeBearerHeader(): String = this.substringAfter("Bearer ").trim()
    }
}
