package com.turnin.domain.auth.presentation.route

import com.turnin.common.exception.common.CommonErrorCode
import com.turnin.common.jwt.application.dto.JWTTokenDto
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.id.DisplayId
import com.turnin.common.model.id.UserId
import com.turnin.common.route.Api
import com.turnin.domain.auth.application.dto.LoginResultDto
import com.turnin.domain.auth.application.dto.RegisterResultDto
import com.turnin.domain.auth.application.usecase.AuthAdminUseCases
import com.turnin.domain.auth.exception.AuthErrorCode
import com.turnin.domain.auth.exception.AuthException
import com.turnin.domain.auth.presentation.dto.AdminLoginRequest
import com.turnin.domain.auth.presentation.dto.AdminRegisterRequest
import com.turnin.util.TestClientFactory.createTestClient
import com.turnin.util.testPlugin
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthAdminRouteTest {
    private val route = Api.Admin.Auth
    private val usecase: AuthAdminUseCases = mockk()

    // ------------------------------ 로그인 ------------------------------
    @Test
    fun `관리자 로그인 - 성공 테스트`() = testApplication {
        coEvery { usecase.validateAdminSecretKey(any()) } just Runs
        coEvery { usecase.login(any()) } returns MockLoginResultDto

        val client = createTestClient()
        testPlugin(routing = { authAdminRoutes(route, usecase) })

        val response = client.post("${route.ROUTE}${route.LOGIN}") {
            contentType(ContentType.Application.Json)
            setBody(MockValidAdminLoginRequest)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(response.bodyAsText().contains(MockLoginResultDto.jwtTokenDto.accessToken))
    }

    @Test
    fun `관리자 로그인 - 잘못된 비밀키로 요청 시 실패한다`() = testApplication {
        coEvery { usecase.validateAdminSecretKey(any()) } throws AuthException.Unauthorized()

        val client = createTestClient()
        testPlugin(routing = { authAdminRoutes(route, usecase) })

        val response = client.post("${route.ROUTE}${route.LOGIN}") {
            contentType(ContentType.Application.Json)
            setBody(MockInvalidSecretKeyLoginRequest)
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `관리자 로그인 - 요청 바디 유효성 검사를 실패하는 경우 BadRequest를 반환한다`() = testApplication {
        coEvery { usecase.validateAdminSecretKey(any()) } just Runs

        val client = createTestClient()
        testPlugin(routing = { authAdminRoutes(route, usecase) })

        val response = client.post("${route.ROUTE}${route.LOGIN}") {
            contentType(ContentType.Application.Json)
            setBody(MockInvalidAdminLoginRequest)
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains(CommonErrorCode.ValidationDefault.code))
    }

    @Test
    fun `관리자 로그인 - 잘못된 형식의 요청 바디인 경우 BadRequest를 반환한다`() = testApplication {
        val client = createTestClient()
        testPlugin(routing = { authAdminRoutes(route, usecase) })

        val response = client.post("${route.ROUTE}${route.LOGIN}") {
            contentType(ContentType.Application.Json)
            setBody("""{ "weirdField": "asd" }""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains(CommonErrorCode.MalformedRequest.description))
    }

    @Test
    fun `관리자 로그인 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        coEvery { usecase.validateAdminSecretKey(any()) } just Runs
        coEvery { usecase.login(any()) } throws Exception("Something went wrong")

        val client = createTestClient()
        testPlugin(routing = { authAdminRoutes(route, usecase) })

        val response = client.post("${route.ROUTE}${route.LOGIN}") {
            contentType(ContentType.Application.Json)
            setBody(MockValidAdminLoginRequest)
        }

        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertTrue(response.bodyAsText().contains("${HttpStatusCode.InternalServerError.value}"))
    }

    // ------------------------------ 회원가입 ------------------------------

    @Test
    fun `관리자 회원가입 - 성공 테스트`() = testApplication {
        coEvery { usecase.validateAdminSecretKey(any()) } just Runs
        coEvery { usecase.existsDisplayId(DisplayId(MockValidAdminRegisterRequest.displayId)) } returns false
        coEvery { usecase.register(any()) } returns MockRegisterResultDto

        val client = createTestClient()
        testPlugin(routing = { authAdminRoutes(route, usecase) })

        val response = client.post("${route.ROUTE}${route.REGISTER}") {
            contentType(ContentType.Application.Json)
            setBody(MockValidAdminRegisterRequest)
        }

        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(response.bodyAsText().contains(MockRegisterResultDto.jwtTokenDto.accessToken))
    }

    @Test
    fun `관리자 회원가입 - 잘못된 비밀키로 요청 시 실패한다`() = testApplication {
        coEvery { usecase.validateAdminSecretKey(any()) } throws AuthException.Unauthorized()

        val client = createTestClient()
        testPlugin(routing = { authAdminRoutes(route, usecase) })

        val response = client.post("${route.ROUTE}${route.REGISTER}") {
            contentType(ContentType.Application.Json)
            setBody(MockInvalidSecretKeyRequest)
        }

        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `관리자 회원가입 - 이미 존재하는 displayId로 요청 시 Conflict를 반환한다`() = testApplication {
        coEvery { usecase.validateAdminSecretKey(any()) } just Runs
        coEvery { usecase.existsDisplayId(DisplayId(MockValidAdminRegisterRequest.displayId)) } returns true

        val client = createTestClient()
        testPlugin(routing = { authAdminRoutes(route, usecase) })

        val response = client.post("${route.ROUTE}${route.REGISTER}") {
            contentType(ContentType.Application.Json)
            setBody(MockValidAdminRegisterRequest)
        }
        val responseBody = response.bodyAsText()

        assertEquals(HttpStatusCode.Conflict, response.status)
        assertTrue(responseBody.contains(AuthErrorCode.UserDuplicated.code))
    }

    @Test
    fun `관리자 회원가입 - 요청 바디 유효성 검사를 실패하는 경우 BadRequest를 반환한다`() = testApplication {
        coEvery { usecase.validateAdminSecretKey(any()) } just Runs

        val client = createTestClient()
        testPlugin(routing = { authAdminRoutes(route, usecase) })

        val response = client.post("${route.ROUTE}${route.REGISTER}") {
            contentType(ContentType.Application.Json)
            setBody(MockInvalidAdminRegisterRequest)
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains(CommonErrorCode.ValidationDefault.code))
    }

    @Test
    fun `관리자 회원가입 - 잘못된 형식의 요청 바디인 경우 BadRequest를 반환한다`() = testApplication {
        val client = createTestClient()
        testPlugin(routing = { authAdminRoutes(route, usecase) })

        val response = client.post("${route.ROUTE}${route.REGISTER}") {
            contentType(ContentType.Application.Json)
            setBody("""{ "weirdField": "asd" }""")
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(response.bodyAsText().contains(CommonErrorCode.MalformedRequest.description))
    }

    @Test
    fun `관리자 회원가입 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        coEvery { usecase.validateAdminSecretKey(any()) } just Runs
        coEvery { usecase.existsDisplayId(DisplayId(MockValidAdminRegisterRequest.displayId)) } returns false
        coEvery { usecase.register(any()) } throws Exception("Something went wrong")

        val client = createTestClient()
        testPlugin(routing = { authAdminRoutes(route, usecase) })

        val response = client.post("${route.ROUTE}${route.REGISTER}") {
            contentType(ContentType.Application.Json)
            setBody(MockValidAdminRegisterRequest)
        }

        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertTrue(response.bodyAsText().contains("${HttpStatusCode.InternalServerError.value}"))
    }

    companion object {
        private val MockUserId = UserId(1L)
        private val MockJWTTokenDto = JWTTokenDto(
            accessToken = "aaa.bbb.ccc",
            refreshToken = "aaa.bbb.ccc",
        )
        private val MockRegisterResultDto = RegisterResultDto(
            userId = MockUserId,
            jwtTokenDto = MockJWTTokenDto,
        )
        private val MockValidAdminRegisterRequest = AdminRegisterRequest(
            secretKey = "valid-secret-key",
            provider = SocialLoginProvider.GOOGLE,
            providerId = "providerIDDDDD",
            displayId = "admin_123",
            name = "admin",
            profileImageUrl = "http://example.com/profile.jpg",
            introduce = "Hello!",
        )
        private val MockInvalidSecretKeyRequest = MockValidAdminRegisterRequest.copy(
            secretKey = "invalid-secret-key",
        )
        private val MockInvalidAdminRegisterRequest = MockValidAdminRegisterRequest.copy(
            displayId = "",
            name = "",
        )
        private val MockLoginResultDto = LoginResultDto(
            userId = MockUserId,
            jwtTokenDto = MockJWTTokenDto,
        )
        private val MockValidAdminLoginRequest = AdminLoginRequest(
            secretKey = "valid-secret-key",
            provider = SocialLoginProvider.GOOGLE,
            providerId = "providerIDDDDD",
        )
        private val MockInvalidSecretKeyLoginRequest = MockValidAdminLoginRequest.copy(
            secretKey = "invalid-secret-key",
        )
        private val MockInvalidAdminLoginRequest = MockValidAdminLoginRequest.copy(
            providerId = "",
        )
    }
}
