package com.peekr.common.jwt.infrastructure

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.peekr.common.jwt.domain.model.entity.JWTToken
import com.peekr.common.jwt.domain.model.entity.JWTTokenPayload
import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.common.jwt.exception.TokenException
import com.peekr.common.util.PeekrDateTime
import com.peekr.common.util.PeekrDateTime.toDate
import com.peekr.common.util.config.AppConfig
import io.ktor.util.logging.KtorSimpleLogger

private typealias JWTChecksum = Pair<String, String>

class JWTTokenServiceImpl(private val appConfig: AppConfig) : JWTTokenService {
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
        appConfig.get("ktor.security.jwt.refreshTokenExpiresIn")?.toLong() ?: 0L
    }

    private val secretKey by lazy {
        appConfig.getOrDefault("ktor.security.jwt.secret", "jwt-secret")
    }

    private val algorithm = Algorithm.HMAC256(secretKey)

    override fun generate(payload: JWTTokenPayload): JWTToken {
        try {
            val accessToken = createAccessToken(payload, accessTokenExpiresIn)
            val refreshToken = createRefreshToken(payload, refreshTokenExpiresIn)

            return JWTToken(accessToken, refreshToken)
        } catch (e: Exception) {
            throw TokenException.CannotCreateToken(e.message)
        }
    }

    override fun createVerifier(): JWTVerifier = try {
        JWT
            .require(algorithm)
            .withAudience(audience)
            .withIssuer(issuer)
            .build()
    } catch (e: Exception) {
        throw TokenException.CannotCreateTokenVerifier(e.message)
    }

    private fun createAccessToken(
        payload: JWTTokenPayload,
        expiresIn: Long,
    ): String {
        val checksum = createRandomChecksum()
        val now = PeekrDateTime.now()
        val issuedAt = now.toDate()
        val expiresAt = now.plusMillis(expiresIn).toDate()

        return JWT
            .create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withSubject(payload.userId)
            .withClaim(payload.claimName.name, payload.claim)
            .withClaim(checksum.first, checksum.second)
            .withIssuedAt(issuedAt)
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }

    private fun createRefreshToken(
        payload: JWTTokenPayload,
        expiresIn: Long,
    ): String {
        val checksum = createRandomChecksum()
        val now = PeekrDateTime.now()
        val issuedAt = now.toDate()
        val expiresAt = now.plusMillis(expiresIn).toDate()

        return JWT
            .create()
            .withSubject(payload.userId)
            .withJWTId(checksum.second)
            .withIssuedAt(issuedAt)
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }

    private fun createRandomChecksum(): JWTChecksum {
        val randomValue = PeekrDateTime.now().toEpochMilli().toString()
        return JWTChecksum("checksum", randomValue)
    }
}

private val LOGGER = KtorSimpleLogger(JWTTokenServiceImpl::class.simpleName ?: "JWTTokenServiceImpl")
