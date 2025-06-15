package com.peekr.common.jwt.exception

import com.peekr.common.exception.ApiErrorCode

sealed class TokenErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    data object InvalidToken :
        TokenErrorCode(raw = T001, description = "Invalid token")

    data object InvalidVerifier :
        TokenErrorCode(raw = T002, description = "Invalid verifier")

    data object GenerateTokenError :
        TokenErrorCode(raw = T003, description = "cannot generate token")
}

private const val T001 = "T001"
private const val T002 = "T002"
private const val T003 = "T003"
