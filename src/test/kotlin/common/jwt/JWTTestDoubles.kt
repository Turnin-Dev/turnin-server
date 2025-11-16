package com.peekr.common.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.peekr.common.jwt.JWTTestDoubles.ACCESS_TOKEN_EXPIRES_IN
import com.peekr.common.jwt.JWTTestDoubles.AUDIENCE
import com.peekr.common.jwt.JWTTestDoubles.ISSUER
import com.peekr.common.jwt.JWTTestDoubles.MockAlgorithm
import com.peekr.common.jwt.domain.model.JWTClaimName
import com.peekr.common.jwt.domain.model.JWTToken
import com.peekr.common.jwt.domain.model.JWTTokenPayload
import java.time.Instant
import java.util.Date

internal object JWTTestDoubles {
    const val SECRET = "-aaaaa-aaaaa-aaaaa-aaaaa-aaaaa"
    const val ISSUER = "test-issuer"
    const val AUDIENCE = "test-audience"
    const val REALM = "test-realm"
    const val ACCESS_TOKEN_EXPIRES_IN = 3600000L // 1 hour
    const val REFRESH_TOKEN_EXPIRES_IN = 86400000L // 1 day

    val MockAlgorithm: Algorithm = Algorithm.HMAC256(SECRET)
    val MockVerifier = JWT
        .require(MockAlgorithm)
        .withAudience(AUDIENCE)
        .withIssuer(ISSUER)
        .build()
    val MockRefreshTokenVerifier = JWT
        .require(MockAlgorithm)
        .build()

    fun getJWTTokenPayload(
        subject: String = "user123",
        claimName: JWTClaimName = JWTClaimName.DISPLAY_ID,
        claim: String = "DISPLAY_ID123",
    ): JWTTokenPayload = JWTTokenPayload(
        userId = subject,
        claimName = claimName,
        claim = claim,
    )

    fun getMockJWTToken(
        payload: JWTTokenPayload = getJWTTokenPayload(),
    ): JWTToken = generateTestToken(payload)

    fun getMockJWTToken(subject: String): JWTToken =
        generateTestToken(getJWTTokenPayload(subject))
}

private fun generateTestToken(payload: JWTTokenPayload): JWTToken {
    val now = Instant.now()
    val accessToken = JWT
        .create()
        .withAudience(AUDIENCE)
        .withIssuer(ISSUER)
        .withSubject(payload.userId)
        .withClaim(payload.claimName.name, payload.claim)
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(now.plusMillis(ACCESS_TOKEN_EXPIRES_IN)))
        .sign(MockAlgorithm)
    val refreshToken = JWT
        .create()
        .withSubject(payload.userId)
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(now.plusMillis(ACCESS_TOKEN_EXPIRES_IN)))
        .sign(MockAlgorithm)

    return JWTToken(accessToken, refreshToken)
}
