package com.peekr.common.jwt.domain.service

import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.interfaces.DecodedJWT
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
     * JWT Token 타입에 맞게 토큰에서 Subject를 추출한다.
     *
     * @param token JWT Token
     * @param type JWT Token 타입
     *
     * @throws TokenException.TokenExpiredException 토큰 만료 시
     * @throws TokenException.CannotCreateTokenVerifier Verifier 생성 실패 시
     * @throws TokenException.VerificationFailedException 토큰 검증 실패 시
     * @throws TokenException.CannotDecodedException 토큰 디코딩 실패 시
     *
     * @return 토큰의 Subject
     */
    fun extractSubjectWithToken(token: String, type: JWTTokenType): String?

    /**
     * 토큰을 검증한다.
     *
     * @param token JWT 토큰
     * @param type 토큰 타입
     *
     * @throws TokenException.TokenExpiredException 토큰 만료 시
     * @throws TokenException.CannotCreateTokenVerifier Verifier 생성 실패 시
     * @throws TokenException.VerificationFailedException 토큰 검증 실패 시
     * @throws TokenException.CannotDecodedException 토큰 디코딩 실패 시
     *
     * @return [DecodedJWT]
     */
    fun verify(token: String, type: JWTTokenType): DecodedJWT?
}
