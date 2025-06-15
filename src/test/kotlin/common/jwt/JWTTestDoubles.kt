package com.peekr.common.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.peekr.common.jwt.JWTTestDoubles.ACCESS_TOKEN_EXPIRES_IN
import com.peekr.common.jwt.JWTTestDoubles.AUDIENCE
import com.peekr.common.jwt.JWTTestDoubles.ISSUER
import com.peekr.common.jwt.JWTTestDoubles.MockAlgorithm
import com.peekr.common.jwt.JWTTestDoubles.MockJWTTokenPayload
import com.peekr.common.jwt.JWTTestDoubles.REFRESH_TOKEN_EXPIRES_IN
import com.peekr.common.jwt.domain.model.entity.JWTToken
import com.peekr.common.jwt.domain.model.entity.JWTTokenPayload
import com.peekr.common.jwt.domain.model.entity.JWTVerifierConfig
import com.peekr.common.jwt.domain.model.value.JWTClaimName
import io.ktor.server.config.ApplicationConfig
import io.mockk.every
import io.mockk.mockk
import java.time.Instant
import java.util.Date

internal object JWTTestDoubles {
    const val SECRET = "-aaaaa-aaaaa-aaaaa-aaaaa-aaaaa"
    const val ISSUER = "test-issuer"
    const val AUDIENCE = "test-audience"
    const val REALM = "test-realm"
    const val ACCESS_TOKEN_EXPIRES_IN = 3600000L // 1 hour
    const val REFRESH_TOKEN_EXPIRES_IN = 86400000L // 1 day

    val MockJWTTokenPayload = JWTTokenPayload(
        subject = "user123",
        claimName = JWTClaimName.Name,
        claim = "USER",
    )

    val MockApplicationConfig = mockk<ApplicationConfig> {
        every { propertyOrNull("ktor.security.jwt.realm")?.getString() } returns REALM
        every { propertyOrNull("ktor.security.jwt.issuer")?.getString() } returns ISSUER
        every { propertyOrNull("ktor.security.jwt.audience")?.getString() } returns AUDIENCE
        every { propertyOrNull("ktor.security.jwt.secret")?.getString() } returns SECRET
        every {
            propertyOrNull("ktor.security.jwt.accessTokenExpiresIn")?.getString()
        } returns ACCESS_TOKEN_EXPIRES_IN.toString()
        every {
            propertyOrNull("ktor.security.jwt.refreshTokenExpiresIn")?.getString()
        } returns REFRESH_TOKEN_EXPIRES_IN.toString()
    }

    val MockJWTVerifierConfig = JWTVerifierConfig(
        secretKey = SECRET,
        audience = AUDIENCE,
        issuer = ISSUER,
    )
    val MockAlgorithm = Algorithm.HMAC256(SECRET)
    val MockVerifier = JWT
        .require(MockAlgorithm)
        .withAudience(MockJWTVerifierConfig.audience)
        .withIssuer(MockJWTVerifierConfig.issuer)
        .build()
    val MockJWTToken = generateTestToken()
}

private fun generateTestToken(): JWTToken {
    val now = Instant.now()
    val accessToken = JWT
        .create()
        .withAudience(AUDIENCE)
        .withIssuer(ISSUER)
        .withSubject(MockJWTTokenPayload.subject)
        .withClaim(MockJWTTokenPayload.claimName.name, MockJWTTokenPayload.claim)
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(now.plusMillis(ACCESS_TOKEN_EXPIRES_IN)))
        .sign(MockAlgorithm)
    val refreshToken = JWT
        .create()
        .withSubject(MockJWTTokenPayload.subject)
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(now.plusMillis(REFRESH_TOKEN_EXPIRES_IN)))
        .sign(MockAlgorithm)

    return JWTToken(accessToken, refreshToken)
}
