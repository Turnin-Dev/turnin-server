package com.peekr.domain.auth.presentation.route

import com.peekr.common.api.Api
import com.peekr.common.exception.CommonErrorCode
import com.peekr.common.exception.toErrorResponse
import com.peekr.common.validator.CommonValidator
import com.peekr.common.validator.CommonValidator.validationUserIdAndReturn
import com.peekr.domain.auth.AuthTestDoubles.MockInvalidLoginRequest
import com.peekr.domain.auth.AuthTestDoubles.MockInvalidRegisterRequest
import com.peekr.domain.auth.AuthTestDoubles.MockJWTTokenDto
import com.peekr.domain.auth.AuthTestDoubles.MockValidLoginRequest
import com.peekr.domain.auth.AuthTestDoubles.MockValidRegisterRequest
import com.peekr.domain.auth.application.dto.FindUserResultDto
import com.peekr.domain.auth.application.usecase.AuthUseCase
import com.peekr.domain.auth.exception.AuthErrorCode
import com.peekr.util.TestClientFactory.createTestClient
import com.peekr.util.testPlugin
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.After
import org.junit.Before
import org.junit.Test

class AuthRouteTest {
    private val authUseCase: AuthUseCase = mockk()

    @Before
    fun setup() {
        mockkObject(CommonValidator)
        every { validationUserIdAndReturn(any<Long>()) } returns 1L
        every { validationUserIdAndReturn(any<String>()) } returns 1L
        coEvery { authUseCase.extractUserId(any()) } returns 0L
    }

    @After
    fun teardown() {
        unmockkObject(CommonValidator)
    }

    @Test
    fun `login 성공 테스트`() = testApplication {
        // given
        val route = Api.V1.Auth
        val client = createTestClient()
        coEvery { authUseCase.login(any()) } returns MockJWTTokenDto

        testPlugin(
            routing = { authRoutes(route, authUseCase) },
        )

        // when
        val loginEndPoint = "${route.ROUTE}${route.LOGIN}"
        val response = client.post(loginEndPoint) {
            contentType(ContentType.Application.Json)
            setBody(MockValidLoginRequest)
        }
        val responseBody = response.bodyAsText()

        // then
        coVerify(exactly = 1) { authUseCase.login(any()) }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(responseBody.contains(MockJWTTokenDto.accessToken))
    }

    @Test
    fun `login 실패 테스트 - 요청 바디 유효성 검사를 실패하는 경우`() = testApplication {
        // given
        val route = Api.V1.Auth
        val client = createTestClient()
        coEvery { authUseCase.login(any()) } returns MockJWTTokenDto

        testPlugin(
            routing = { authRoutes(route, authUseCase) },
        )

        // when
        val loginEndPoint = "${route.ROUTE}${route.LOGIN}"
        val response = client.post(loginEndPoint) {
            contentType(ContentType.Application.Json)
            setBody(MockInvalidLoginRequest)
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(responseBody.contains(CommonErrorCode.Validation.code))
    }

    @Test
    fun `login 실패 테스트 - 잘못된 요청 바디를 호출하는 경우`() = testApplication {
        // given
        val route = Api.V1.Auth
        val client = createTestClient()
        coEvery { authUseCase.login(any()) } returns MockJWTTokenDto

        testPlugin(
            routing = { authRoutes(route, authUseCase) },
        )

        // when
        val loginEndPoint = "${route.ROUTE}${route.LOGIN}"
        val response = client.post(loginEndPoint) {
            contentType(ContentType.Application.Json)
            setBody(
                """
                {
                    "weirdField": "asd"
                }
                """.trimIndent(),
            )
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(responseBody.contains(CommonErrorCode.MalformedRequest.description))
    }

    @Test
    fun `login 예외 테스트 - AuthUseCase에서 예외가 발생하는 경우`() = testApplication {
        // given
        val route = Api.V1.Auth
        val client = createTestClient()
        coEvery {
            authUseCase.login(any())
        } throws IllegalArgumentException()

        testPlugin(
            routing = { authRoutes(route, authUseCase) },
        )

        // when
        val loginEndPoint = "${route.ROUTE}${route.LOGIN}"
        val response = client.post(loginEndPoint) {
            contentType(ContentType.Application.Json)
            setBody(MockValidLoginRequest)
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertTrue(responseBody.contains("${HttpStatusCode.InternalServerError.value}"))
    }

    @Test
    fun `login 실패 테스트 - 토큰이 Null을 반환하는 경우`() = testApplication {
        // given
        val route = Api.V1.Auth
        val client = createTestClient()
        coEvery {
            authUseCase.login(any())
        } returns null

        testPlugin(
            routing = { authRoutes(route, authUseCase) },
        )

        // when
        val loginEndPoint = "${route.ROUTE}${route.LOGIN}"
        val response = client.post(loginEndPoint) {
            contentType(ContentType.Application.Json)
            setBody(MockValidLoginRequest)
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(
            responseBody.contains(
                AuthErrorCode.LoginFailed.toErrorResponse(HttpStatusCode.BadRequest).message,
            ),
        )
    }

    @Test
    fun `register 성공 테스트`() = testApplication {
        // given
        val route = Api.V1.Auth
        val client = createTestClient()
        coEvery { authUseCase.register(any()) } returns MockJWTTokenDto
        testPlugin(
            routing = { authRoutes(route, authUseCase) },
        )

        // when
        val registerEndPoint = "${route.ROUTE}${route.REGISTER}"
        val response = client.post(registerEndPoint) {
            contentType(ContentType.Application.Json)
            setBody(MockValidRegisterRequest)
        }
        val responseBody = response.bodyAsText()

        // then
        coVerify(exactly = 1) { authUseCase.register(any()) }
        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(responseBody.contains(MockJWTTokenDto.accessToken))
    }

    @Test
    fun `register 실패 테스트 - 요청 바디 유효성 검사를 실패하는 경우`() = testApplication {
        // given
        val route = Api.V1.Auth
        val client = createTestClient()
        coEvery { authUseCase.register(any()) } returns MockJWTTokenDto
        testPlugin(
            routing = { authRoutes(route, authUseCase) },
        )

        // when
        val registerEndPoint = "${route.ROUTE}${route.REGISTER}"
        val response = client.post(registerEndPoint) {
            contentType(ContentType.Application.Json)
            setBody(MockInvalidRegisterRequest)
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(responseBody.contains(CommonErrorCode.Validation.code))
    }

    @Test
    fun `register 실패 테스트 - AuthUseCase에서 예외가 발생하는 경우`() = testApplication {
        // given
        val route = Api.V1.Auth
        val client = createTestClient()
        coEvery {
            authUseCase.register(any())
        } throws IllegalArgumentException()
        testPlugin(
            routing = { authRoutes(route, authUseCase) },
        )

        // when
        val registerEndPoint = "${route.ROUTE}${route.REGISTER}"
        val response = client.post(registerEndPoint) {
            contentType(ContentType.Application.Json)
            setBody(MockValidRegisterRequest)
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertTrue(responseBody.contains("${HttpStatusCode.InternalServerError.value}"))
    }

    @Test
    fun `refresh 성공 테스트`() = testApplication {
        // given
        val route = Api.V1.Auth
        val client = createTestClient()
        coEvery { authUseCase.refresh(any(), any()) } returns MockJWTTokenDto
        testPlugin(
            routing = { authRoutes(route, authUseCase) },
        )

        // when
        val refreshEndPoint = "${route.ROUTE}${route.REFRESH}"
        val response = client.get(refreshEndPoint) {
            headers.append("Authorization", "Bearer ${MockJWTTokenDto.refreshToken}")
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(responseBody.contains(MockJWTTokenDto.accessToken))
        assertTrue(responseBody.contains(MockJWTTokenDto.refreshToken))
    }

    @Test
    fun `refresh 실패 테스트 - 요청 헤더에 토큰이 없는 경우`() = testApplication {
        // given
        val route = Api.V1.Auth
        val client = createTestClient()
        coEvery { authUseCase.refresh(any(), any()) } returns MockJWTTokenDto
        testPlugin(
            routing = { authRoutes(route, authUseCase) },
        )

        // when
        val refreshEndPoint = "${route.ROUTE}${route.REFRESH}"
        val response = client.get(refreshEndPoint)
        val responseBody = response.bodyAsText()

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(responseBody.contains(CommonErrorCode.EmptyRequestHeader.description))
    }

    @Test
    fun `refresh 실패 테스트 - 요청 헤더에 토큰 형식이 잘못된 경우`() = testApplication {
        // given
        val route = Api.V1.Auth
        val client = createTestClient()
        coEvery { authUseCase.refresh(any(), any()) } returns MockJWTTokenDto
        testPlugin(
            routing = { authRoutes(route, authUseCase) },
        )

        // when
        val refreshEndPoint = "${route.ROUTE}${route.REFRESH}"
        val response = client.get(refreshEndPoint) {
            headers.append("Authorization", "Is Token?")
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(responseBody.contains(CommonErrorCode.Validation.code))
    }

    @Test
    fun `refresh 실패 테스트 - AuthUseCase(refresh())에서 null을 반환하는 경우`() = testApplication {
        // given
        val route = Api.V1.Auth
        val client = createTestClient()
        coEvery { authUseCase.refresh(any(), any()) } returns null
        testPlugin(
            routing = { authRoutes(route, authUseCase) },
        )

        // when
        val refreshEndPoint = "${route.ROUTE}${route.REFRESH}"
        val response = client.get(refreshEndPoint) {
            headers.append("Authorization", "Bearer ${MockJWTTokenDto.refreshToken}")
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(HttpStatusCode.Unauthorized, response.status)
        assertTrue(responseBody.contains(AuthErrorCode.RefreshTokenExpired.code))
        assertTrue(responseBody.contains(AuthErrorCode.RefreshTokenExpired.description))
    }

    @Test
    fun `existUser 성공 테스트 - 사용자가 존재하는 경우`() = testApplication {
        // given
        val route = Api.V1.Auth
        val client = createTestClient()
        coEvery { authUseCase.findUser(any(), any()) } returns FindUserResultDto(true)
        testPlugin(
            routing = { authRoutes(route, authUseCase) },
        )

        // when
        val existUserEndPoint = "${route.ROUTE}${route.EXISTS_USER}/GOOGLE/123123"
        val response = client.get(existUserEndPoint)
        val responseBody = response.bodyAsText()

        // then
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(responseBody.contains(true.toString()))
    }

    @Test
    fun `existUser 실패 테스트 - 유효하지 않은 provider 값인 경우`() = testApplication {
        // given
        val route = Api.V1.Auth
        val client = createTestClient()
        coEvery { authUseCase.findUser(any(), any()) } returns FindUserResultDto(true)
        testPlugin(
            routing = { authRoutes(route, authUseCase) },
        )

        // when
        val existUserEndPoint = "${route.ROUTE}${route.EXISTS_USER}/a/a"
        val response = client.get(existUserEndPoint)
        val responseBody = response.bodyAsText()

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(
            responseBody.contains(
                AuthErrorCode.PathParameterInvalid("provider").description,
            ),
        )
    }
}
