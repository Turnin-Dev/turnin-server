package com.peekr.common.jwt

import com.peekr.common.exception.ErrorResponse
import com.peekr.common.jwt.JWTTestDoubles.AUDIENCE
import com.peekr.common.jwt.JWTTestDoubles.ISSUER
import com.peekr.common.jwt.JWTTestDoubles.REALM
import com.peekr.common.jwt.domain.model.entity.JWTToken
import com.peekr.common.jwt.domain.service.JWTTokenService
import com.peekr.common.jwt.exception.TokenException
import com.peekr.common.jwt.infrastructure.JWTConfigFactory
import com.peekr.util.TestSerialization.decode
import com.peekr.util.testPlugin
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.response.respondText
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.testing.testApplication
import io.mockk.every
import io.mockk.mockk
import kotlin.test.assertEquals
import org.junit.Before
import org.junit.Test

class JWTSecurityTest {
    private lateinit var jwtConfigFactory: JWTConfigFactory
    private lateinit var jwtTokenService: JWTTokenService

    @Before
    fun setUp() {
        jwtConfigFactory = mockk {
            every { createAlgorithm(any()) } returns JWTTestDoubles.MockAlgorithm
            every { createVerifier(any()) } returns JWTTestDoubles.MockVerifier
        }

        jwtTokenService = mockk {
            every { realm } returns REALM
            every { audience } returns AUDIENCE
            every { issuer } returns ISSUER
            every { generate(any()) } returns JWTTestDoubles.getMockJWTToken()
            every { getVerifierConfig() } returns JWTTestDoubles.MockVerifierConfig
        }
    }

    @Test
    fun `인증이 필요한 엔드포인트 성공 테스트`() = testApplication {
        // Given
        testPlugin(
            module = testJwtModule,
            plugin = { configureJwtSecurity() },
            routing = { protectedRoute() },
        )
        val token = jwtTokenService.generate(JWTTestDoubles.getJWTTokenPayload())

        // When
        val response = client.get(TEST_ENDPOINT) {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
        }

        // Then
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(TEST_RESPONSE, response.bodyAsText())
    }

    @Test
    fun `인증이 필요한 엔드포인트 예외 테스트`() = testApplication {
        // Given
        testPlugin(
            module = testJwtModule,
            plugin = { configureJwtSecurity() },
            routing = { protectedRoute() },
        )
        val invalidToken = "Iam.invalid.token"
        val jwtToken = JWTToken(invalidToken, invalidToken)
        every { jwtTokenService.generate(any()) } returns jwtToken

        // When
        val response = client.get(TEST_ENDPOINT) {
            headers.remove(HttpHeaders.Authorization)
            header(HttpHeaders.Authorization, "Bearer $invalidToken")
        }

        // Then
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertEquals(
            TEST_ERROR_CODE,
            response.bodyAsText().decode<ErrorResponse>().code,
        )
    }

    private val testJwtModule = org.koin.dsl.module {
        single<JWTTokenService> { jwtTokenService }
        single<JWTConfigFactory> { jwtConfigFactory }
    }

    private fun Route.protectedRoute() {
        authenticate {
            get(TEST_ENDPOINT) {
                call.respondText(TEST_RESPONSE)
            }
        }
    }

    companion object {
        private const val TEST_ENDPOINT = "/protected"
        private const val TEST_RESPONSE = "Access Granted"
        private val TEST_ERROR_CODE = TokenException.InvalidTokenException().errorCode.code
    }
}
