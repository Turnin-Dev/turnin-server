package com.peekr.common.jwt.exception

import com.peekr.common.exception.ApiErrorCode
import com.peekr.common.exception.RawErrorCodeFactory.toErrorCode

sealed class TokenErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    data object InvalidToken :
        TokenErrorCode(raw = HEADER.toErrorCode(1), description = "Invalid token")

    data object InvalidVerifier :
        TokenErrorCode(raw = HEADER.toErrorCode(2), description = "Invalid verifier")
}

private const val HEADER = "T"
