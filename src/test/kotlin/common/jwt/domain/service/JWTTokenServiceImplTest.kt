package com.peekr.common.jwt.domain.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.peekr.common.jwt.JWTTestDoubles
import com.peekr.common.jwt.JWTTestDoubles.AUDIENCE
import com.peekr.common.jwt.JWTTestDoubles.ISSUER
import com.peekr.common.jwt.JWTTestDoubles.SECRET
import com.peekr.common.jwt.JWTTestDoubles.TestApplicationConfig
import com.peekr.common.jwt.infrastructure.JWTConfigFactory
import com.peekr.common.jwt.infrastructure.JWTTokenServiceImpl
import com.peekr.common.util.AppConfig
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test

class JWTTokenServiceImplTest {
    private lateinit var appConfig: AppConfig
    private lateinit var jwtConfigFactory: JWTConfigFactory
    private lateinit var jwtTokenService: JWTTokenService

    @Before
    fun setup() {
        // mock AppConfig
        appConfig = mockk {
            every { applicationConfiguration } returns TestApplicationConfig
        }

        jwtConfigFactory = mockk {
            every { createAlgorithm(SECRET) } returns Algorithm.HMAC256(SECRET)
        }

        jwtTokenService = JWTTokenServiceImpl(appConfig, jwtConfigFactory)
    }

    @Test
    fun `generate should create valid access and refresh tokens`() {
        // given
        val payload = JWTTestDoubles.TestPayload

        // when
        val token = jwtTokenService.generate(payload)

        // then
        val decodedAccessToken = JWT.decode(token.accessToken)
        val decodedRefreshToken = JWT.decode(token.refreshToken)

        assertEquals(decodedAccessToken.subject, payload.subject)
        assertEquals(decodedAccessToken.getClaim(payload.claimName.name).asString(), payload.claim)
        assertEquals(decodedAccessToken.issuer, ISSUER)
        assert(AUDIENCE in decodedAccessToken.audience)

        assertEquals(decodedRefreshToken.subject, payload.subject)
    }

    @Test
    fun `getVerifierConfig should return correct config values`() {
        val config = jwtTokenService.getVerifierConfig()

        assertEquals(config.secretKey, SECRET)
        assertEquals(config.audience, AUDIENCE)
        assertEquals(config.issuer, ISSUER)
    }
}
