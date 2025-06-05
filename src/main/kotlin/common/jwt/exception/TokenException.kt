package com.peekr.common.jwt.exception

sealed class TokenException(
    message: String,
) : RuntimeException(message) {
    class InvalidTokenException(
        message: String,
    ) : TokenException(message)
}
