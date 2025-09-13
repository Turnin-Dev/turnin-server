package com.peekr.common.jwt

import com.peekr.common.validator.PeekrValidator.validation

object JWTValidator {
    fun validate(token: String) {
        val parts = token.split(" ")
        validation(parts.first() == "Bearer") {
            "$COMMON_TOKEN_ERROR (Ex. Bearer로 시작해야 함)"
        }
        validation(parts.size == 2) {
            "$COMMON_TOKEN_ERROR (Ex. Bearer [토큰내용])"
        }
    }

    private const val COMMON_TOKEN_ERROR = "JWT 토큰 형식이 올바르지 않습니다."
}
