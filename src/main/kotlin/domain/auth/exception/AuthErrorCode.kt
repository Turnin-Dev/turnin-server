package com.peekr.domain.auth.exception

import com.peekr.common.exception.ApiErrorCode
import com.peekr.common.exception.RawErrorCodeFactory.toErrorCode

sealed class AuthErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    data object LoginFailed :
        AuthErrorCode(HEADER.toErrorCode(1), "login failed")
}

private const val HEADER = "A"
