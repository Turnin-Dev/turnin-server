package com.peekr.common.jwt.domain.service

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.peekr.common.jwt.JWTTestDoubles
import com.peekr.common.jwt.JWTTestDoubles.ACCESS_TOKEN_EXPIRES_IN
import com.peekr.common.jwt.JWTTestDoubles.AUDIENCE
import com.peekr.common.jwt.JWTTestDoubles.ISSUER
import com.peekr.common.jwt.JWTTestDoubles.REALM
import com.peekr.common.jwt.JWTTestDoubles.REFRESH_TOKEN_EXPIRES_IN
import com.peekr.common.jwt.JWTTestDoubles.SECRET
import com.peekr.common.jwt.infrastructure.JWTConfigFactory
import com.peekr.common.jwt.infrastructure.JWTTokenServiceImpl
import com.peekr.common.util.config.AppConfig
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test

class JWTTokenServiceImplTest {
    private val appConfigManager: AppConfig = mockk()
    private lateinit var jwtConfigFactory: JWTConfigFactory
    private lateinit var jwtTokenService: JWTTokenService

    @Before
    fun setup() {
        every { appConfigManager.get(any()) } answers {
            val key = firstArg<String>()
            when (key) {
                "ktor.security.jwt.realm" -> REALM
                "ktor.security.jwt.issuer" -> ISSUER
                "ktor.security.jwt.audience" -> AUDIENCE
                "ktor.security.jwt.secret" -> SECRET
                "ktor.security.jwt.accessTokenExpiresIn" -> ACCESS_TOKEN_EXPIRES_IN.toString()
                "ktor.security.jwt.refreshTokenExpiresIn" -> REFRESH_TOKEN_EXPIRES_IN.toString()
                else -> "default-value"
            }
        }

        every { appConfigManager.getOrDefault(any(), any()) } answers {
            val key = firstArg<String>()
            val defaultValue = secondArg<String>()
            when (key) {
                "ktor.security.jwt.realm" -> REALM
                "ktor.security.jwt.issuer" -> ISSUER
                "ktor.security.jwt.audience" -> AUDIENCE
                "ktor.security.jwt.secret" -> SECRET
                "ktor.security.jwt.accessTokenExpiresIn" -> ACCESS_TOKEN_EXPIRES_IN.toString()
                "ktor.security.jwt.refreshTokenExpiresIn" -> REFRESH_TOKEN_EXPIRES_IN.toString()
                else -> defaultValue
            }
        }

        jwtConfigFactory = mockk {
            every { createAlgorithm(SECRET) } returns Algorithm.HMAC256(SECRET)
        }

        jwtTokenService = JWTTokenServiceImpl(appConfigManager, jwtConfigFactory)
    }

    @Test
    fun `generate should create valid access and refresh tokens`() {
        // given
        val payload = JWTTestDoubles.getJWTTokenPayload()

        // when
        val token = jwtTokenService.generate(payload)

        // then
        val decodedAccessToken = JWT.decode(token.accessToken)
        val decodedRefreshToken = JWT.decode(token.refreshToken)

        assertEquals(decodedAccessToken.subject, payload.userId)
        assertEquals(decodedAccessToken.getClaim(payload.claimName.name).asString(), payload.claim)
        assertEquals(decodedAccessToken.issuer, ISSUER)
        assert(AUDIENCE in decodedAccessToken.audience)

        assertEquals(decodedRefreshToken.subject, payload.userId)
    }

    @Test
    fun `getVerifierConfig should return correct config values`() {
        val config = jwtTokenService.getVerifierConfig()

        assertEquals(config.secretKey, SECRET)
        assertEquals(config.audience, AUDIENCE)
        assertEquals(config.issuer, ISSUER)
    }
}
