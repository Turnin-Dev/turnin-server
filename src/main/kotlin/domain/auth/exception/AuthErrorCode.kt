package com.peekr.domain.auth.exception

import com.peekr.common.exception.ApiErrorCode

sealed class AuthErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    data object LoginFailed :
        AuthErrorCode(A001, "login failed")
}

private const val A001 = "A001"
