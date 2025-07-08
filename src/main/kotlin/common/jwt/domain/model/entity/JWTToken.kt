package com.peekr.common.jwt.domain.model.entity

import com.peekr.common.validator.PeekrValidator.validation

enum class JWTTokenType {
    Access,
    Refresh,
}

data class JWTToken(
    val accessToken: String,
    val refreshToken: String,
) {
    companion object {
        fun validate(token: String) {
            validation(token.split(" ").first() == "Bearer") {
                "$COMMON_TOKEN_ERROR (Ex. Bearer로 시작해야 함)"
            }
            validation(token.split(" ").size == 2) {
                "$COMMON_TOKEN_ERROR (Ex. Bearer [토큰내용])"
            }
        }

        fun String.removeBearerHeader(): String = this.substringAfter("Bearer ").trim()

        private const val COMMON_TOKEN_ERROR = "JWT 토큰 형식이 올바르지 않습니다."
    }
}
