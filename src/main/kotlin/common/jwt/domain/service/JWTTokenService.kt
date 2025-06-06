package com.peekr.common.jwt.domain.service

import com.peekr.common.jwt.domain.model.entity.JWTToken
import com.peekr.common.jwt.domain.model.entity.JWTVerifierConfig
import com.peekr.common.jwt.domain.model.entity.JwtTokenPayload

/** JWT Token 을 생성하고 검증에 필요한 정보를 제공한다. */
interface JWTTokenService {
    /**
     * [JwtTokenPayload]를 기반으로 JWT Token을 생성한다.
     *
     * @param payload 토큰 생성에 필요한 정보
     */
    fun generate(payload: JwtTokenPayload): JWTToken

    /** JWT Token을 검증하고 JWT Verifier를 생성하기 위한 정보를 반환한다.*/
    fun getVerifierConfig(): JWTVerifierConfig
}
