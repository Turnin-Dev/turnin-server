package com.peekr.common.jwt.infrastructure

import com.auth0.jwt.JWT
import com.peekr.common.jwt.domain.model.entity.JWTToken
import com.peekr.common.jwt.domain.model.entity.JWTTokenPayload
import com.peekr.common.jwt.domain.model.entity.JWTVerifierConfig
import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.common.jwt.exception.TokenException
import com.peekr.common.util.PeekrDateTime
import com.peekr.common.util.config.AppConfig
import java.time.Instant
import java.util.Date

private typealias JWTChecksum = Pair<String, String>

class JWTTokenServiceImpl(
    private val appConfig: AppConfig,
    jwtConfigFactory: JWTConfigFactory,
) : JWTTokenService {
    override val realm by lazy {
        appConfig.getOrDefault("ktor.security.jwt.realm", "jwt-realm")
    }

    override val audience by lazy {
        appConfig.getOrDefault("ktor.security.jwt.audience", "jwt-audience")
    }

    override val issuer by lazy {
        appConfig.getOrDefault("ktor.security.jwt.issuer", "jwt-issuer")
    }

    private val accessTokenExpiresIn by lazy {
        appConfig.get("ktor.security.jwt.accessTokenExpiresIn")?.toLong() ?: 0L
    }

    private val refreshTokenExpiresIn by lazy {
        appConfig.get("ktor.security.jwt.accessTokenExpiresIn")?.toLong() ?: 0L
    }

    private val secretKey by lazy {
        appConfig.getOrDefault("ktor.security.jwt.secret", "jwt-secret")
    }

    private val now = Instant.now()
    val algorithm = jwtConfigFactory.createAlgorithm(secretKey)

    override fun generate(payload: JWTTokenPayload): JWTToken {
        try {
            val accessToken = createAccessToken(payload, accessTokenExpiresIn)
            val refreshToken = createRefreshToken(payload, refreshTokenExpiresIn)

            return JWTToken(accessToken, refreshToken)
        } catch (e: Exception) {
            throw TokenException.CannotCreateToken(e.message)
        }
    }

    override fun getVerifierConfig(): JWTVerifierConfig {
        try {
            return JWTVerifierConfig(secretKey, audience, issuer)
        } catch (e: Exception) {
            throw TokenException.CannotCreateTokenVerifier(e.message)
        }
    }

    private fun createAccessToken(
        payload: JWTTokenPayload,
        expiresIn: Long,
    ): String {
        val checksum = createRandomChecksum()

        return JWT
            .create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withSubject(payload.subject)
            .withClaim(payload.claimName.name, payload.claim)
            .withClaim(checksum.first, checksum.second)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusMillis(expiresIn)))
            .sign(algorithm)
    }

    private fun createRefreshToken(
        payload: JWTTokenPayload,
        expiresIn: Long,
    ): String {
        val checksum = createRandomChecksum()

        return JWT
            .create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim(payload.claimName.name, payload.claim)
            .withClaim(checksum.first, checksum.second)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusMillis(expiresIn)))
            .sign(algorithm)
    }

    private fun createRandomChecksum(): JWTChecksum {
        val randomValue = PeekrDateTime.now().toEpochMilli().toString()
        return JWTChecksum("checksum", randomValue)
    }
}
