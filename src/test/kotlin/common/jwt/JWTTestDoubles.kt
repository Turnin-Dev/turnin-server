package com.peekr.common.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.peekr.common.jwt.JWTTestDoubles.ACCESS_TOKEN_EXPIRES_IN
import com.peekr.common.jwt.JWTTestDoubles.AUDIENCE
import com.peekr.common.jwt.JWTTestDoubles.ISSUER
import com.peekr.common.jwt.JWTTestDoubles.MockAlgorithm
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

    val MockVerifierConfig = JWTVerifierConfig(
        secretKey = SECRET,
        audience = AUDIENCE,
        issuer = ISSUER,
    )
    val MockAlgorithm = Algorithm.HMAC256(SECRET)
    val MockVerifier = JWT
        .require(MockAlgorithm)
        .withAudience(MockVerifierConfig.audience)
        .withIssuer(MockVerifierConfig.issuer)
        .build()

    fun getJWTTokenPayload(
        subject: String = "user123",
        claimName: JWTClaimName = JWTClaimName.Name,
        claim: String = "USER",
    ): JWTTokenPayload = JWTTokenPayload(
        subject = subject,
        claimName = claimName,
        claim = claim,
    )

    fun getMockJWTToken(
        payload: JWTTokenPayload = getJWTTokenPayload(),
    ): JWTToken = generateTestToken(payload)
}

private fun generateTestToken(payload: JWTTokenPayload): JWTToken {
    val now = Instant.now()
    val accessToken = JWT
        .create()
        .withAudience(AUDIENCE)
        .withIssuer(ISSUER)
        .withSubject(payload.subject)
        .withClaim(payload.claimName.name, payload.claim)
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(now.plusMillis(ACCESS_TOKEN_EXPIRES_IN)))
        .sign(MockAlgorithm)
    val refreshToken = JWT
        .create()
        .withAudience(AUDIENCE)
        .withIssuer(ISSUER)
        .withSubject(payload.subject)
        .withClaim(payload.claimName.name, payload.claim)
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(now.plusMillis(REFRESH_TOKEN_EXPIRES_IN)))
        .sign(MockAlgorithm)

    return JWTToken(accessToken, refreshToken)
}
