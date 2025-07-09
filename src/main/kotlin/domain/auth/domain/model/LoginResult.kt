package com.peekr.domain.auth.domain.model

import com.peekr.common.jwt.domain.model.JWTToken

/**
 * 로그인 후 얻게되는 결과 값
 *
 * @param jwtToken 생성된 JWT Token
 * @param authUser 로그인된 사용자 정보
 */
data class LoginResult(
    val jwtToken: JWTToken,
    val authUser: AuthUser,
)
