package com.peekr.domain.auth.domain.model.domain.auth.domain.model

import com.peekr.common.jwt.domain.model.entity.JWTToken
import com.peekr.domain.auth.domain.model.AuthUser

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
