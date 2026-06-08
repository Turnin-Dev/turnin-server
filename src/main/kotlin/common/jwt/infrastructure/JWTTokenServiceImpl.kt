package com.turnin.common.jwt.infrastructure

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTCreationException
import com.auth0.jwt.exceptions.JWTDecodeException
import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.exceptions.TokenExpiredException
import com.auth0.jwt.interfaces.DecodedJWT
import com.turnin.common.jwt.domain.model.JWTToken
import com.turnin.common.jwt.domain.model.JWTTokenPayload
import com.turnin.common.jwt.domain.model.JWTTokenType
import com.turnin.common.jwt.domain.service.JWTTokenService
import com.turnin.common.jwt.exception.TokenException
import com.turnin.common.util.TurninDateTime
import com.turnin.common.util.config.AppConfig
import java.util.UUID

class JWTTokenServiceImpl(
    private val appConfig: AppConfig,
    private val secretKey: String,
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
        appConfig.get("ktor.security.jwt.refreshTokenExpiresIn")?.toLong() ?: 0L
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
        val checksum = UUID.randomUUID().toString()
        val issuedAt = TurninDateTime.now()
        val expiresAt = issuedAt.plusMillis(expiresIn)

        return JWT
            .create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withSubject(payload.userId)
            .apply {
                payload.claims.forEach { (claimName, value) ->
                    withClaim(claimName.key, value)
                }
            }.withJWTId(checksum)
            .withIssuedAt(issuedAt)
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }

    private fun createRefreshToken(
        userId: String,
        expiresIn: Long,
    ): String {
        val checksum = UUID.randomUUID().toString()
        val issuedAt = TurninDateTime.now()
        val expiresAt = issuedAt.plusMillis(expiresIn)

        return JWT
            .create()
            .withSubject(userId)
            .withJWTId(checksum)
            .withIssuedAt(issuedAt)
            .withExpiresAt(expiresAt)
            .sign(algorithm)
    }

    override fun extractSubjectWithToken(token: String, type: JWTTokenType): String? = try {
        createVerifier(type)
            .verify(token)
            .subject
    } catch (e: TokenExpiredException) {
        throw TokenException.TokenExpiredException(e)
    } catch (e: JWTDecodeException) {
        throw TokenException.CannotDecodedException(e)
    } catch (e: JWTVerificationException) {
        throw TokenException.VerificationFailedException(e)
    }

    override fun verify(
        token: String,
        type: JWTTokenType,
    ): DecodedJWT? = try {
        createVerifier(type).verify(token)
    } catch (e: TokenExpiredException) {
        throw TokenException.TokenExpiredException(e)
    } catch (e: JWTDecodeException) {
        throw TokenException.CannotDecodedException(e)
    } catch (e: JWTVerificationException) {
        throw TokenException.VerificationFailedException(e)
    }
}
