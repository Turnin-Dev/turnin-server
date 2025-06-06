package com.peekr.common.jwt.exception

import com.peekr.common.exception.DefaultErrorCode
import com.peekr.common.exception.DefaultException

sealed class TokenException(
    message: String,
) : DefaultException(DefaultErrorCode.Token, message) {
    class InvalidTokenException(
        message: String,
    ) : TokenException(message)
}
