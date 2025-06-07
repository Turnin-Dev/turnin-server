package com.peekr.common.jwt.domain.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.peekr.common.jwt.domain.model.entity.JWTTokenPayload
import com.peekr.common.jwt.domain.model.value.JWTClaimName
import com.peekr.common.jwt.infrastructure.JWTConfigFactory
import com.peekr.common.jwt.infrastructure.JWTTokenServiceImpl
import com.peekr.common.util.AppConfig
import io.ktor.server.config.ApplicationConfig
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test

class JWTTokenServiceImplTest {
    private lateinit var appConfig: AppConfig
    private lateinit var jwtConfigFactory: JWTConfigFactory
    private lateinit var jwtTokenService: JWTTokenService

    private val secret = "-aaaaa-aaaaa-aaaaa-aaaaa-aaaaa"
    private val issuer = "test-issuer"
    private val audience = "test-audience"
    private val realm = "test-realm"
    private val accessTokenExpiresIn = 3600000L // 1 hour
    private val refreshTokenExpiresIn = 86400000L // 1 day

    @Before
    fun setup() {
        // mock AppConfig
        val applicationConfig = mockk<ApplicationConfig> {
            every { propertyOrNull("ktor.security.jwt.realm")?.getString() } returns realm
            every { propertyOrNull("ktor.security.jwt.issuer")?.getString() } returns issuer
            every { propertyOrNull("ktor.security.jwt.audience")?.getString() } returns audience
            every { propertyOrNull("ktor.security.jwt.secret")?.getString() } returns secret
            every {
                propertyOrNull("ktor.security.jwt.accessTokenExpiresIn")?.getString()
            } returns accessTokenExpiresIn.toString()
            every {
                propertyOrNull("ktor.security.jwt.refreshTokenExpiresIn")?.getString()
            } returns refreshTokenExpiresIn.toString()
        }

        appConfig = mockk {
            every { applicationConfiguration } returns applicationConfig
        }

        jwtConfigFactory = mockk {
            every { createAlgorithm(secret) } returns Algorithm.HMAC256(secret)
        }

        jwtTokenService = JWTTokenServiceImpl(appConfig, jwtConfigFactory)
    }

    @Test
    fun `generate should create valid access and refresh tokens`() {
        // given
        val payload = JWTTokenPayload(
            subject = "user123",
            claimName = JWTClaimName.Name,
            claim = "USER",
        )

        // when
        val token = jwtTokenService.generate(payload)

        // then
        val decodedAccessToken = JWT.decode(token.accessToken)
        val decodedRefreshToken = JWT.decode(token.refreshToken)

        assertEquals(decodedAccessToken.subject, payload.subject)
        assertEquals(decodedAccessToken.getClaim(payload.claimName.name).asString(), payload.claim)
        assertEquals(decodedAccessToken.issuer, issuer)
        assert(audience in decodedAccessToken.audience)

        assertEquals(decodedRefreshToken.subject, payload.subject)
    }

    @Test
    fun `getVerifierConfig should return correct config values`() {
        val config = jwtTokenService.getVerifierConfig()

        assertEquals(config.secretKey, secret)
        assertEquals(config.audience, audience)
        assertEquals(config.issuer, issuer)
    }
}
