package com.peekr.common.jwt.infrastructure.serviceImpl

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.JWTVerifier
import com.peekr.common.jwt.domain.model.entity.JwtToken
import com.peekr.common.jwt.domain.model.entity.JwtTokenPayload
import com.peekr.common.jwt.exception.TokenException
import com.peekr.common.util.AppConfig
import java.time.Instant
import java.util.Date
import org.koin.core.annotation.Single

/** JWT Token 을 생성하고 검증하는 클래스 */
@Single
class JwtTokenService(
    private val appConfig: AppConfig,
) {
    val jwtName by lazy {
        appConfig.applicationConfiguration.propertyOrNull("ktor.security.jwt.name")?.getString()
            ?: "jwt-name"
    }

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
    private val algorithm = Algorithm.HMAC256(secretKey)

    /** [JwtTokenPayload]를 기반으로 JWT Token을 생성한다. */
    fun generate(payload: JwtTokenPayload): JwtToken {
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

        return JwtToken(accessToken, refreshToken)
    }

    /**
     * JWT Token을 검증하고 [JWTVerifier]를 반환한다.
     * @throws TokenException.InvalidTokenException - 토큰 검증 과정에서 예외가 발생 했을 시
     */
    fun verify(): JWTVerifier = try {
        JWT
            .require(algorithm)
            .withAudience(audience)
            .withIssuer(issuer)
            .build()
    } catch (e: Exception) {
        throw TokenException.InvalidTokenException("Invalid token")
    }
}
