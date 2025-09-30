package com.peekr.domain.user.presentation.route

import com.peekr.common.exception.ApiException
import com.peekr.common.exception.CommonErrorCode
import com.peekr.common.jwt.JWTTestDoubles
import com.peekr.common.model.DisplayId
import com.peekr.common.model.Name
import com.peekr.common.model.UserId
import com.peekr.common.route.Api
import com.peekr.domain.user.UserTestDoubles.MockUserDto
import com.peekr.domain.user.application.dto.UserPatchDto
import com.peekr.domain.user.application.usecase.UserUseCases
import com.peekr.domain.user.presentation.dto.UserPatchRequest
import com.peekr.util.TestClientFactory.createTestClient
import com.peekr.util.testPlugin
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.path
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Test

class UserRoutesTest {
    private val route = Api.V1.User
    private val userUseCases = mockk<UserUseCases>()

    @Test
    fun `사용자 조회 GET 요청 성공 테스트`() = testApplication {
        // given
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken("1")
        coEvery { userUseCases.get(TestUserId) } returns MockUserDto

        testPlugin(
            authRouting = { userRoutes(route, userUseCases) },
        )

        // when
        val getUserEndPoint = "${route.ROUTE}/${TestUserId.value}"
        val response = client.get(getUserEndPoint) {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(responseBody.contains(MockUserDto.name.value))
        assertTrue(responseBody.contains(MockUserDto.displayId.value))
        assertTrue(responseBody.contains(MockUserDto.role.name))
    }

    @Test
    fun `사용자 조회 GET 요청 실패 테스트 -잘못된 형식의 사용자 ID인 경우 BadRequest를 반환한다`() = testApplication {
        // given
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery { userUseCases.get(TestUserId) } returns MockUserDto

        testPlugin(
            authRouting = { userRoutes(route, userUseCases) },
        )

        invalidUserIds.forEach { invalidUserId ->
            // when
            val getUserEndPoint = "${route.ROUTE}/$invalidUserId"
            val response = client.get(getUserEndPoint) {
                header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            }

            // then
            assertEquals(HttpStatusCode.BadRequest, response.status)
        }
    }

    @Test
    fun `사용자 조회 GET 요청 실패 테스트 - 사용자가 존재하지 않는 경우 NotFound를 반환한다`() = testApplication {
        // given
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken("1")
        coEvery { userUseCases.get(TestUserId) } returns null

        testPlugin(
            authRouting = { userRoutes(route, userUseCases) },
        )

        // when
        val getUserEndPoint = "${route.ROUTE}/${TestUserId.value}"
        val response = client.get(getUserEndPoint) {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertTrue(responseBody.contains("${HttpStatusCode.NotFound.value}"))
    }

    @Test
    fun `사용자 수정 UPDATE 요청 성공 테스트`() = testApplication {
        // given
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery { userUseCases.update(TestUserId, TestUserPatchDto) } returns true

        testPlugin(
            authRouting = { userRoutes(route, userUseCases) },
        )

        // when
        val response = client.patch {
            url {
                path(route.ROUTE)
                appendPathSegments(TestUserId.value.toString())
            }
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestUserPatchRequest)
        }

        // then
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `사용자 수정 UPDATE 요청 실패 테스트 - 잘못된 형식의 사용자 ID인 경우 BadRequest를 반환한다`() = testApplication {
        // given
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery { userUseCases.update(TestUserId, TestUserPatchDto) } returns true

        testPlugin(
            authRouting = { userRoutes(route, userUseCases) },
        )

        // when
        val response = client.patch {
            url {
                path(route.ROUTE)
                appendPathSegments(INVALID_USER_ID)
            }
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestUserPatchRequest)
        }

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `사용자 수정 UPDATE 요청 실패 테스트 - 사용자가 존재하지 않는 경우 NotFound를 반환한다`() = testApplication {
        // given
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery { userUseCases.update(TestUserId, TestUserPatchDto) } returns false

        testPlugin(
            authRouting = { userRoutes(route, userUseCases) },
        )

        // when
        val response = client.patch {
            url {
                path(route.ROUTE)
                appendPathSegments(TestUserId.value.toString())
            }
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestUserPatchRequest)
        }

        // then
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `사용자 수정 UPDATE 요청 실패 테스트 - 토큰 없이 요청 시 401 에러를 반환한다`() = testApplication {
        // given
        val client = createTestClient()
        coEvery { userUseCases.update(TestUserId, TestUserPatchDto) } returns false

        testPlugin(
            authRouting = { userRoutes(route, userUseCases) },
        )

        // when
        val response = client.patch {
            url {
                path(route.ROUTE)
                appendPathSegments(TestUserId.value.toString())
            }
            contentType(ContentType.Application.Json)
            setBody(TestUserPatchRequest)
        }

        // then
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `사용자 수정 UPDATE 요청 실패 테스트 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        // given
        val expectedApiException = ApiException(
            errorCode = CommonErrorCode.Unexpected,
            status = HttpStatusCode.InternalServerError,
            message = "unexpected error",
        )
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery {
            userUseCases.update(TestUserId, TestUserPatchDto)
        } throws expectedApiException

        testPlugin(
            authRouting = { userRoutes(route, userUseCases) },
        )

        // when
        val response = client.patch {
            url {
                path(route.ROUTE)
                appendPathSegments(TestUserId.value.toString())
            }
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestUserPatchRequest)
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(expectedApiException.status, response.status)
        assertTrue(responseBody.contains(expectedApiException.errorCode.code))
        assertTrue(responseBody.contains(expectedApiException.errorCode.description))
    }

    companion object {
        private val TestUserId = UserId(1L)
        private const val INVALID_USER_ID = "asd"
        private val invalidUserIds = listOf(INVALID_USER_ID)
        private val TestUserPatchDto = UserPatchDto(
            displayId = DisplayId("id"),
            name = Name("name"),
            profileImageUrl = null,
            introduce = "",
        )
        private val TestUserPatchRequest = UserPatchRequest(
            displayId = "id",
            name = "name",
            profileImageUrl = null,
            introduce = "",
        )
    }
}
