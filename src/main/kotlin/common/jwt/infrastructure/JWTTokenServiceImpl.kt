package com.peekr.common.jwt.infrastructure

import com.auth0.jwt.JWT
import com.peekr.common.jwt.domain.model.entity.JWTToken
import com.peekr.common.jwt.domain.model.entity.JWTVerifierConfig
import com.peekr.common.jwt.domain.model.entity.JwtTokenPayload
import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.common.util.AppConfig
import java.time.Instant
import java.util.Date

class JWTTokenServiceImpl(
    private val appConfig: AppConfig,
    private val jwtConfigFactory: JWTConfigFactory,
) : JWTTokenService {
    val realm by lazy {
        appConfig.applicationConfiguration.propertyOrNull("ktor.security.jwt.realm")?.getString()
            ?: "jwt-realm"
    }

    val audience by lazy {
        appConfig.applicationConfiguration.propertyOrNull("ktor.security.jwt.audience")?.getString()
            ?: "jwt-audience"
    }

    private val issuer by lazy {
        appConfig.applicationConfiguration.propertyOrNull("ktor.security.jwt.issuer")?.getString()
            ?: "jwt-issuer"
    }

    private val accessTokenExpiresIn by lazy {
        appConfig.applicationConfiguration
            .propertyOrNull(
                "ktor.security.jwt.accessTokenExpiresIn",
            )?.getString()
            ?.toLong()
            ?: 0L
    }

    private val refreshTokenExpiresIn by lazy {
        appConfig.applicationConfiguration
            .propertyOrNull(
                "ktor.security.jwt.accessTokenExpiresIn",
            )?.getString()
            ?.toLong()
            ?: 0L
    }

    private val secretKey by lazy {
        appConfig.applicationConfiguration.propertyOrNull("ktor.security.jwt.secret")?.getString()
            ?: "jwt-secret"
    }

    private val now = Instant.now()
    val algorithm = jwtConfigFactory.createAlgorithm(secretKey)

    override fun generate(payload: JwtTokenPayload): JWTToken {
        val accessToken = JWT
            .create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withSubject(payload.subject)
            .withClaim(payload.claimName.name, payload.claim)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusMillis(accessTokenExpiresIn)))
            .sign(algorithm)

        val refreshToken = JWT
            .create()
            .withSubject(payload.subject)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusMillis(refreshTokenExpiresIn)))
            .sign(algorithm)

        return JWTToken(accessToken, refreshToken)
    }

    override fun getVerifierConfig(): JWTVerifierConfig =
        JWTVerifierConfig(secretKey, audience, issuer)
}
