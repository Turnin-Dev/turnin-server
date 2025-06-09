package com.peekr.common.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.peekr.common.jwt.JWTTestDoubles.ACCESS_TOKEN_EXPIRES_IN
import com.peekr.common.jwt.JWTTestDoubles.AUDIENCE
import com.peekr.common.jwt.JWTTestDoubles.ISSUER
import com.peekr.common.jwt.JWTTestDoubles.REFRESH_TOKEN_EXPIRES_IN
import com.peekr.common.jwt.JWTTestDoubles.TestAlgorithm
import com.peekr.common.jwt.JWTTestDoubles.TestPayload
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

    val TestPayload = JWTTokenPayload(
        subject = "user123",
        claimName = JWTClaimName.Name,
        claim = "USER",
    )

    val TestApplicationConfig = mockk<ApplicationConfig> {
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

    val TestJWTVerifierConfig = JWTVerifierConfig(
        secretKey = SECRET,
        audience = AUDIENCE,
        issuer = ISSUER,
    )
    val TestAlgorithm = Algorithm.HMAC256(SECRET)
    val TestVerifier = JWT
        .require(TestAlgorithm)
        .withAudience(TestJWTVerifierConfig.audience)
        .withIssuer(TestJWTVerifierConfig.issuer)
        .build()
    val TestJWTToken = generateTestToken()
}

private fun generateTestToken(): JWTToken {
    val now = Instant.now()
    val accessToken = JWT
        .create()
        .withAudience(AUDIENCE)
        .withIssuer(ISSUER)
        .withSubject(TestPayload.subject)
        .withClaim(TestPayload.claimName.name, TestPayload.claim)
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(now.plusMillis(ACCESS_TOKEN_EXPIRES_IN)))
        .sign(TestAlgorithm)
    val refreshToken = JWT
        .create()
        .withSubject(TestPayload.subject)
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(now.plusMillis(REFRESH_TOKEN_EXPIRES_IN)))
        .sign(TestAlgorithm)

    return JWTToken(accessToken, refreshToken)
}
