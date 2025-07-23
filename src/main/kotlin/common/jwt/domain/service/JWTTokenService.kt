package com.peekr.common.jwt.domain.service

import com.auth0.jwt.JWTVerifier
import com.peekr.common.jwt.domain.model.JWTToken
import com.peekr.common.jwt.domain.model.JWTTokenPayload
import com.peekr.common.jwt.domain.model.JWTTokenType
import com.peekr.common.jwt.exception.TokenException

/** JWT Token 을 생성하고 검증에 필요한 정보를 제공한다. */
interface JWTTokenService {
    val realm: String
    val audience: String
    val issuer: String

    /**
     * [JWTTokenPayload]를 기반으로 JWT Token을 생성한다.
     *
     * @param payload 토큰 생성에 필요한 정보 ([JWTTokenPayload])
     * @throws TokenException.CannotCreateToken 토큰을 생성하는 과정에서 예외 발생 시
     */
    fun generate(payload: JWTTokenPayload): JWTToken

    /**
     * JWT Verifier를 생성한다.
     *
     * @param type 토큰 타입 ([JWTTokenType])
     * @throws TokenException.CannotCreateTokenVerifier Token Verifier 를 생성하는 과정에서 예외 발생 시
     */
    fun createVerifier(type: JWTTokenType): JWTVerifier

    /**
     * JWT 형식의 토큰에서 String 타입의 UserId를 추출한다.
     *
     * @param token JWT 형식의 토큰
     * @return [Long] UserID, UserID 형식이 아니거나 추출하지 못한다면 null
     * @throws TokenException.CannotDecodedException 토큰이 정상적으로 디코딩 할 수 없는 형식인 경우 예외 발생
     */
    fun extractUserId(token: String): String?
}
