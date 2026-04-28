package com.turnin.domain.auth.domain.model

import com.turnin.common.jwt.domain.model.JWTToken

/**
 * 회원가입 후 얻게되는 결과 값
 *
 * @param jwtToken 생성된 JWT Token
 * @param authUser 회원가입된 사용자 정보
 */
data class RegisterResult(
    val jwtToken: JWTToken,
    val authUser: AuthUser,
)
