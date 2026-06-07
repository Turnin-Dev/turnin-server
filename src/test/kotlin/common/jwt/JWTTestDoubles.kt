package com.turnin.common.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.turnin.common.jwt.JWTTestDoubles.ACCESS_TOKEN_EXPIRES_IN
import com.turnin.common.jwt.JWTTestDoubles.AUDIENCE
import com.turnin.common.jwt.JWTTestDoubles.ISSUER
import com.turnin.common.jwt.JWTTestDoubles.MockAlgorithm
import com.turnin.common.jwt.JWTTestDoubles.REFRESH_TOKEN_EXPIRES_IN
import com.turnin.common.jwt.domain.model.JWTClaimName
import com.turnin.common.jwt.domain.model.JWTToken
import com.turnin.common.jwt.domain.model.JWTTokenPayload
import com.turnin.common.plugin.AuthRole
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

    fun getJWTTokenPayload(
        subject: String = "user123",
        role: AuthRole = AuthRole.USER,
    ): JWTTokenPayload = JWTTokenPayload(
        userId = subject,
        claims = mapOf(
            JWTClaimName.DISPLAY_ID to "DISPLAY_ID123",
            JWTClaimName.ROLE to role.name,
        ),
    )

    fun getMockJWTToken(
        payload: JWTTokenPayload = getJWTTokenPayload(),
    ): JWTToken = generateTestToken(payload)

    fun getMockJWTToken(
        subject: String,
        role: AuthRole = AuthRole.USER,
    ): JWTToken =
        generateTestToken(getJWTTokenPayload(subject, role))

    fun getExpiredRefreshToken(subject: String = "user123"): String {
        val now = Instant.now()
        return JWT
            .create()
            .withSubject(subject)
            .withIssuedAt(Date.from(now.minusMillis(86400000L * 2))) // 2일 전 발급
            .withExpiresAt(Date.from(now.minusMillis(86400000L))) // 1일 전 만료
            .sign(MockAlgorithm)
    }

    fun getTamperedRefreshToken(subject: String = "user123"): String {
        val differentAlgorithm = Algorithm.HMAC256("different-secret-key")
        val now = Instant.now()
        return JWT
            .create()
            .withSubject(subject)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusMillis(REFRESH_TOKEN_EXPIRES_IN)))
            .sign(differentAlgorithm) // 다른 시크릿으로 서명
    }

    fun getExpiredAccessToken(subject: String = "user123"): String {
        val now = Instant.now()
        return JWT
            .create()
            .withAudience(AUDIENCE)
            .withIssuer(ISSUER)
            .withSubject(subject)
            .withIssuedAt(Date.from(now.minusMillis(ACCESS_TOKEN_EXPIRES_IN * 2)))
            .withExpiresAt(Date.from(now.minusMillis(ACCESS_TOKEN_EXPIRES_IN)))
            .sign(MockAlgorithm)
    }

    fun getTamperedAccessToken(subject: String = "user123"): String {
        val differentAlgorithm = Algorithm.HMAC256("different-secret-key")
        val now = Instant.now()
        return JWT
            .create()
            .withAudience(AUDIENCE)
            .withIssuer(ISSUER)
            .withSubject(subject)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusMillis(ACCESS_TOKEN_EXPIRES_IN)))
            .sign(differentAlgorithm)
    }
}

private fun generateTestToken(payload: JWTTokenPayload): JWTToken {
    val now = Instant.now()
    val accessToken = JWT
        .create()
        .withAudience(AUDIENCE)
        .withIssuer(ISSUER)
        .withSubject(payload.userId)
        .apply {
            payload.claims.forEach { (claimName, value) ->
                withClaim(claimName.name, value)
            }
        }.withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(now.plusMillis(ACCESS_TOKEN_EXPIRES_IN)))
        .sign(MockAlgorithm)
    val refreshToken = JWT
        .create()
        .withSubject(payload.userId)
        .withIssuedAt(Date.from(now))
        .withExpiresAt(Date.from(now.plusMillis(REFRESH_TOKEN_EXPIRES_IN)))
        .sign(MockAlgorithm)

    return JWTToken(accessToken, refreshToken)
}
