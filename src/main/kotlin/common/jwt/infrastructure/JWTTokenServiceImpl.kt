package com.peekr.common.jwt.infrastructure

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.peekr.common.jwt.domain.model.JWTToken
import com.peekr.common.jwt.domain.model.JWTTokenPayload
import com.peekr.common.jwt.domain.model.JWTTokenType
import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.common.jwt.exception.TokenException
import com.peekr.common.util.PeekrDateTime
import com.peekr.common.util.config.AppConfig
import java.util.UUID

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
            val refreshToken = createRefreshToken(payload.userId, refreshTokenExpiresIn)

            return JWTToken(accessToken, refreshToken)
        } catch (e: JWTCreationException) {
            throw TokenException.CannotCreateToken(e)
        } catch (e: IllegalArgumentException) {
            throw TokenException.CannotCreateToken(e)
        }
    }

    override fun createVerifier(type: JWTTokenType): JWTVerifier = try {
        when (type) {
            JWTTokenType.Access -> {
                JWT
                    .require(algorithm)
                    .withAudience(audience)
                    .withIssuer(issuer)
                    .build()
            }

            JWTTokenType.Refresh -> {
                JWT
                    .require(algorithm)
                    .build()
            }
        }
    } catch (e: IllegalArgumentException) {
        throw TokenException.CannotCreateTokenVerifier(e)
    }

    private fun createAccessToken(
        payload: JWTTokenPayload,
        expiresIn: Long,
    ): String {
        val checksum = createRandomChecksum()
        val issuedAt = PeekrDateTime.now()
        val expiresAt = issuedAt.plusMillis(expiresIn)

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
        userId: String,
        expiresIn: Long,
    ): String {
        val checksum = createRandomChecksum()
        val issuedAt = PeekrDateTime.now()
        val expiresAt = issuedAt.plusMillis(expiresIn)

        return JWT
            .create()
            .withSubject(userId)
            .withJWTId(checksum.second)
            .withIssuedAt(issuedAt)
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }

    private fun createRandomChecksum(): JWTChecksum {
        val randomValue = UUID.randomUUID().toString()
        return JWTChecksum("checksum", randomValue)
    }
}
