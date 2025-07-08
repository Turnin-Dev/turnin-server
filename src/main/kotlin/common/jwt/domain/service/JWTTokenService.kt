package com.peekr.common.jwt.domain.service

import com.auth0.jwt.JWTVerifier
import com.peekr.common.jwt.domain.model.entity.JWTToken
import com.peekr.common.jwt.domain.model.entity.JWTTokenPayload

/** JWT Token 을 생성하고 검증에 필요한 정보를 제공한다. */
interface JWTTokenService {
    val realm: String
    val audience: String
    val issuer: String

    /**
     * [JWTTokenPayload]를 기반으로 JWT Token을 생성한다.
     *
     * @param payload 토큰 생성에 필요한 정보
     */
    fun generate(payload: JWTTokenPayload): JWTToken

    /** JWT Verifier를 생성한다.*/
    fun createVerifier(): JWTVerifier
}
