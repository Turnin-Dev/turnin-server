package com.peekr.common.jwt.exception

import com.peekr.common.exception.ApiErrorCode

sealed class TokenErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    data object InvalidToken :
        TokenErrorCode(raw = T001, description = "만료된 토큰이거나 잘못된 토큰 형식입니다.")

    data object InvalidVerifier :
        TokenErrorCode(raw = T002, description = "잘못된 토큰 Verifier 입니다.")

    data object GenerateTokenError :
        TokenErrorCode(raw = T003, description = "토큰을 생성할 수 없습니다.")

    data object DecodeTokenError :
        TokenErrorCode(raw = T004, description = "토큰을 디코딩 할 수 없습니다.")

    data object UnauthorizedUser :
        TokenErrorCode(raw = T005, description = "본인만 조회 가능합니다.")

    data object TokenExpired :
        TokenErrorCode(raw = T006, description = "토큰이 만료되었습니다.")
}

private const val T001 = "T001"
private const val T002 = "T002"
private const val T003 = "T003"
private const val T004 = "T004"
private const val T005 = "T005"
private const val T006 = "T006"
