package com.peekr.common.jwt.infrastructure.serviceImpl

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.peekr.common.jwt.domain.model.entity.JwtToken
import com.peekr.common.jwt.domain.service.JwtTokenProvider
import com.peekr.domain.auth.domain.model.entity.AuthUser
import io.github.cdimascio.dotenv.dotenv
import java.time.Instant
import java.util.Date
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

class JwtTokenProviderImpl : JwtTokenProvider {
    private val env = dotenv()
    private val secretKey = env["JWT_SECRET"] ?: "null"
    private val issuer = env["JWT_ISSUER"] ?: "null"
    private val audience = env["JWT_AUDIENCE"] ?: "null"

    private val algorithm = Algorithm.HMAC256(secretKey)

    override fun generate(authUser: AuthUser): JwtToken {
        val now = Instant.now()
        val claim = "${authUser.provider}/${authUser.name}/${authUser.nickname}"

        val accessToken = JWT
            .create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(authUser.id.toString())
            .withClaim("name", claim)
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusMillis(AccessTokenExpiresAt)))
            .sign(algorithm)

        val refreshToken = JWT
            .create()
            .withSubject(authUser.id.toString())
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plusMillis(RefreshTokenExpiresAt)))
            .sign(algorithm)

        return JwtToken(accessToken, refreshToken)
    }
}

private val AccessTokenExpiresAt: Long = 60.minutes.inWholeMilliseconds
private val RefreshTokenExpiresAt: Long = 7.days.inWholeMilliseconds
