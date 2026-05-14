package com.turnin.common.jwt

import com.turnin.common.exception.ErrorResponse
import com.turnin.common.jwt.JWTTestDoubles.AUDIENCE
import com.turnin.common.jwt.JWTTestDoubles.ISSUER
import com.turnin.common.jwt.JWTTestDoubles.MockVerifier
import com.turnin.common.jwt.JWTTestDoubles.REALM
import com.turnin.common.jwt.domain.model.JWTToken
import com.turnin.common.jwt.domain.service.JWTTokenService
import com.turnin.common.jwt.exception.TokenException
import com.turnin.util.TestSerialization.decode
import com.turnin.util.testPlugin
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
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
    private lateinit var jwtTokenService: JWTTokenService

    @Before
    fun setUp() {
        jwtTokenService = mockk {
            every { realm } returns REALM
            every { audience } returns AUDIENCE
            every { issuer } returns ISSUER
            every { generate(any()) } returns JWTTestDoubles.getMockJWTToken()
            every { createVerifier(any()) } returns MockVerifier
        }
    }

    @Test
    fun `인증이 필요한 엔드포인트 성공 테스트`() = testApplication {
        // Given
        val testToken = JWTTestDoubles.getMockJWTToken()
        every { jwtTokenService.generate(any()) } returns testToken
        testPlugin(
            module = testJwtModule,
            authRouting = { protectedRoute() },
        )
        val token = jwtTokenService.generate(JWTTestDoubles.getJWTTokenPayload())

        // When
        val response = client.get(TEST_ENDPOINT) {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
        }

        // Then
        assertEquals(testToken, token)
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(TEST_RESPONSE, response.bodyAsText())
    }

    @Test
    fun `인증이 필요한 엔드포인트 예외 테스트`() = testApplication {
        // Given
        testPlugin(
            module = testJwtModule,
            authRouting = { protectedRoute() },
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
    }

    private fun Route.protectedRoute() {
        get(TEST_ENDPOINT) {
            call.respondText(TEST_RESPONSE)
        }
    }

    companion object {
        private const val TEST_ENDPOINT = "/protected"
        private const val TEST_RESPONSE = "Access Granted"
        private val TEST_ERROR_CODE = TokenException.InvalidTokenException().errorCode.code
    }
}
