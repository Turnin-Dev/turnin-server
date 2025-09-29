package com.peekr.domain.userKeyword.presentation.route

import com.peekr.common.exception.ApiException
import com.peekr.common.exception.CommonErrorCode
import com.peekr.common.jwt.JWTTestDoubles
import com.peekr.common.model.KeywordId
import com.peekr.common.model.UserId
import com.peekr.common.model.UserKeywordId
import com.peekr.common.route.Api
import com.peekr.domain.userKeyword.application.dto.CreateUserKeywordDto
import com.peekr.domain.userKeyword.application.dto.UserKeywordDto
import com.peekr.domain.userKeyword.application.dto.UserKeywordPatchDto
import com.peekr.domain.userKeyword.application.usecase.UserKeywordUseCases
import com.peekr.domain.userKeyword.presentation.dto.CreateUserKeywordRequest
import com.peekr.domain.userKeyword.presentation.dto.GetUserKeywordResponse
import com.peekr.domain.userKeyword.presentation.dto.PatchUserKeywordRequest
import com.peekr.util.TestClientFactory.createTestClient
import com.peekr.util.testPlugin
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import org.junit.Test

private class TestApiException(message: String) :
    ApiException(
        errorCode = CommonErrorCode.MalformedRequest,
        status = HttpStatusCode.InternalServerError,
        message = message,
    )

class UserKeywordRouteTest {
    private val userKeywordUseCases: UserKeywordUseCases = mockk()

    @Test
    fun `사용자 ID로 사용자 키워드 목록 조회 - 요청 성공 테스트`() = testApplication {
        // given
        val route = Api.V1.UserKeyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        val itemCount = 2
        coEvery {
            userKeywordUseCases.get(TestUserId)
        } returns List(itemCount) { TestUserKeywordDto }

        testPlugin(
            authRouting = { userKeywordRoutes(route, userKeywordUseCases) },
        )

        // when
        val endpoint = "${route.ROUTE}/${TestUserId.value}"
        val response = client.get(endpoint) {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
        }
        val responseBody = response.bodyAsText()
        val userKeywords = Json.decodeFromString<GetUserKeywordResponse>(responseBody)

        // then
        assertTrue(userKeywords.keywords.size == itemCount)
        assertTrue(responseBody.contains("${TestUserKeywordDto.userId}"))
        assertTrue(responseBody.contains("${TestUserKeywordDto.keywordId}"))
    }

    @Test
    fun `사용자 ID로 사용자 키워드 목록 조회 - 토큰 에러 발생 시 HTTP 상태코드 401을 반환한다`() = testApplication {
        // given
        val route = Api.V1.UserKeyword
        val client = createTestClient()
        val itemCount = 2
        coEvery {
            userKeywordUseCases.get(TestUserId)
        } returns List(itemCount) { TestUserKeywordDto }

        testPlugin(
            authRouting = { userKeywordRoutes(route, userKeywordUseCases) },
        )

        // when
        val endpoint = "${route.ROUTE}/${TestUserId.value}"
        val response = client.get(endpoint) {
            contentType(ContentType.Application.Json)
        }

        // then
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `사용자 ID로 사용자 키워드 목록 조회 - 알 수 없는 예외가 발생하는 경우 HTTP 상태코드 500을 반환한다`() = testApplication {
        // given
        val route = Api.V1.UserKeyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery {
            userKeywordUseCases.get(TestUserId)
        } throws Exception("")

        testPlugin(
            authRouting = { userKeywordRoutes(route, userKeywordUseCases) },
        )

        // when
        val endpoint = "${route.ROUTE}/${TestUserId.value}"
        val response = client.get(endpoint) {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
        }

        // then
        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }

    @Test
    fun `사용자 ID로 사용자 키워드 목록 조회 - 알려진 예외가 발생하는 경우 정해진 메시지를 반환할 수 있다`() = testApplication {
        // given
        val route = Api.V1.UserKeyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        val expectedMessage = "error!"
        val expectedException = TestApiException(expectedMessage)
        coEvery {
            userKeywordUseCases.get(TestUserId)
        } throws expectedException

        testPlugin(
            authRouting = { userKeywordRoutes(route, userKeywordUseCases) },
        )

        // when
        val endpoint = "${route.ROUTE}/${TestUserId.value}"
        val response = client.get(endpoint) {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(expectedException.status, response.status)
        assertTrue(responseBody.contains(expectedException.errorCode.code))
    }

    @Test
    fun `사용자 키워드 생성 - 요청 성공 테스트`() = testApplication {
        // given
        val route = Api.V1.UserKeyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery {
            userKeywordUseCases.create(TestCreateUserKeywordDto)
        } returns TestUserKeywordDto

        testPlugin(
            authRouting = { userKeywordRoutes(route, userKeywordUseCases) },
        )

        // when
        val endpoint = route.ROUTE
        val response = client.post(endpoint) {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestCreateUserKeywordRequest)
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(responseBody.contains("${TestUserKeywordDto.userId}"))
        assertTrue(responseBody.contains("${TestUserKeywordDto.keywordId}"))
    }

    @Test
    fun `사용자 키워드 생성 - 알 수 없는 예외가 발생하는 경우 HTTP 상태코드 500을 반환한다`() = testApplication {
        // given
        val route = Api.V1.UserKeyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery {
            userKeywordUseCases.create(TestCreateUserKeywordDto)
        } throws Exception()

        testPlugin(
            authRouting = { userKeywordRoutes(route, userKeywordUseCases) },
        )

        // when
        val endpoint = route.ROUTE
        val response = client.post(endpoint) {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestCreateUserKeywordRequest)
        }

        // then
        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }

    @Test
    fun `사용자 키워드 생성 - 알려진 예외가 발생하는 경우 정해진 메시지를 반환할 수 있다`() = testApplication {
        // given
        val route = Api.V1.UserKeyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        val expectedMessage = "error!"
        val expectedException = TestApiException(expectedMessage)
        coEvery {
            userKeywordUseCases.create(TestCreateUserKeywordDto)
        } throws expectedException

        testPlugin(
            authRouting = { userKeywordRoutes(route, userKeywordUseCases) },
        )

        // when
        val endpoint = route.ROUTE
        val response = client.post(endpoint) {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestCreateUserKeywordRequest)
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(expectedException.status, response.status)
        assertTrue(responseBody.contains(expectedException.errorCode.code))
    }

    @Test
    fun `사용자 키워드 수정 - 요청 성공 테스트`() = testApplication {
        // given
        val route = Api.V1.UserKeyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery {
            userKeywordUseCases.update(
                ownerId = TestUserId,
                userKeywordId = TestUserKeywordId,
                patch = TestUserKeywordPatchDto,
            )
        } returns true

        testPlugin(
            authRouting = { userKeywordRoutes(route, userKeywordUseCases) },
        )

        // when
        val endpoint = route.ROUTE
        val response = client.patch(endpoint) {
            url {
                parameters.append("ownerId", TestUserId.value.toString())
                parameters.append("userKeywordId", TestUserKeywordId.value.toString())
            }
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestPatchUserKeywordRequest)
        }

        // then
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `사용자 키워드 수정 - 수정 실패한 경우 HTTP 상태코드 404를 반환한다`() = testApplication {
        // given
        val route = Api.V1.UserKeyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery {
            userKeywordUseCases.update(
                ownerId = TestUserId,
                userKeywordId = TestUserKeywordId,
                patch = TestUserKeywordPatchDto,
            )
        } returns false

        testPlugin(
            authRouting = { userKeywordRoutes(route, userKeywordUseCases) },
        )

        // when
        val endpoint = route.ROUTE
        val response = client.patch(endpoint) {
            url {
                parameters.append("ownerId", TestUserId.value.toString())
                parameters.append("userKeywordId", TestUserKeywordId.value.toString())
            }
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestPatchUserKeywordRequest)
        }

        // then
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `사용자 키워드 수정 - 알 수 없는 예외가 발생하는 경우 HTTP 상태코드 500을 반환한다`() = testApplication {
        // given
        val route = Api.V1.UserKeyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery {
            userKeywordUseCases.update(
                ownerId = TestUserId,
                userKeywordId = TestUserKeywordId,
                patch = TestUserKeywordPatchDto,
            )
        } throws Exception()

        testPlugin(
            authRouting = { userKeywordRoutes(route, userKeywordUseCases) },
        )

        // when
        val endpoint = route.ROUTE
        val response = client.patch(endpoint) {
            url {
                parameters.append("ownerId", TestUserId.value.toString())
                parameters.append("userKeywordId", TestUserKeywordId.value.toString())
            }
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestPatchUserKeywordRequest)
        }

        // then
        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }

    @Test
    fun `사용자 키워드 수정 - 알려진 예외가 발생하는 경우 정해진 메시지를 반환할 수 있다`() = testApplication {
        // given
        val route = Api.V1.UserKeyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        val expectedMessage = "error!"
        val expectedException = TestApiException(expectedMessage)
        coEvery {
            userKeywordUseCases.update(
                ownerId = TestUserId,
                userKeywordId = TestUserKeywordId,
                patch = TestUserKeywordPatchDto,
            )
        } throws expectedException

        testPlugin(
            authRouting = { userKeywordRoutes(route, userKeywordUseCases) },
        )

        // when
        val endpoint = route.ROUTE
        val response = client.patch(endpoint) {
            url {
                parameters.append("ownerId", TestUserId.value.toString())
                parameters.append("userKeywordId", TestUserKeywordId.value.toString())
            }
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestPatchUserKeywordRequest)
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(expectedException.status, response.status)
        assertTrue(responseBody.contains(expectedException.errorCode.code))
    }

    @Test
    fun `사용자 키워드 삭제 - 요청 성공 테스트`() = testApplication {
        // given
        val route = Api.V1.UserKeyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery {
            userKeywordUseCases.delete(TestUserId, TestUserKeywordId)
        } returns true

        testPlugin(
            authRouting = { userKeywordRoutes(route, userKeywordUseCases) },
        )

        // when
        val endpoint = route.ROUTE
        val response = client.delete(endpoint) {
            url {
                parameters.append("ownerId", TestUserId.value.toString())
                parameters.append("userKeywordId", TestUserKeywordId.value.toString())
            }
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
        }

        // then
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun `사용자 키워드 삭제 - 삭제 실패한 경우 HTTP 상태코드 404를 반환한다`() = testApplication {
        // given
        val route = Api.V1.UserKeyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery {
            userKeywordUseCases.delete(TestUserId, TestUserKeywordId)
        } returns false

        testPlugin(
            authRouting = { userKeywordRoutes(route, userKeywordUseCases) },
        )

        // when
        val endpoint = route.ROUTE
        val response = client.delete(endpoint) {
            url {
                parameters.append("ownerId", TestUserId.value.toString())
                parameters.append("userKeywordId", TestUserKeywordId.value.toString())
            }
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
        }

        // then
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `사용자 키워드 삭제 - 알 수 없는 예외가 발생하는 경우 HTTP 상태코드 500을 반환한다`() = testApplication {
        // given
        val route = Api.V1.UserKeyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery {
            userKeywordUseCases.delete(TestUserId, TestUserKeywordId)
        } throws Exception()

        testPlugin(
            authRouting = { userKeywordRoutes(route, userKeywordUseCases) },
        )

        // when
        val endpoint = route.ROUTE
        val response = client.delete(endpoint) {
            url {
                parameters.append("ownerId", TestUserId.value.toString())
                parameters.append("userKeywordId", TestUserKeywordId.value.toString())
            }
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
        }

        // then
        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }

    @Test
    fun `사용자 키워드 삭제 - 알려진 예외가 발생하는 경우 정해진 메시지를 반환할 수 있다`() = testApplication {
        // given
        val route = Api.V1.UserKeyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        val expectedMessage = "error!"
        val expectedException = TestApiException(expectedMessage)
        coEvery {
            userKeywordUseCases.delete(TestUserId, TestUserKeywordId)
        } throws expectedException

        testPlugin(
            authRouting = { userKeywordRoutes(route, userKeywordUseCases) },
        )

        // when
        val endpoint = route.ROUTE
        val response = client.delete(endpoint) {
            url {
                parameters.append("ownerId", TestUserId.value.toString())
                parameters.append("userKeywordId", TestUserKeywordId.value.toString())
            }
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(expectedException.status, response.status)
        assertTrue(responseBody.contains(expectedException.errorCode.code))
    }

    companion object {
        private val TestUserId = UserId(1L)
        private val TestKeywordId = KeywordId(1L)
        private val TestUserKeywordId = UserKeywordId(1L)
        private const val TEST_OFFSET = 0.0f
        private const val TEST_DESC = "Test description"
        private val TestUserKeywordDto = UserKeywordDto(
            id = TestUserKeywordId.value,
            keywordId = TestKeywordId.value,
            userId = TestUserId.value,
            offsetX = TEST_OFFSET,
            offsetY = TEST_OFFSET,
            description = TEST_DESC,
            createdAt = 1000,
            updatedAt = 1000,
        )
        private val TestCreateUserKeywordDto = CreateUserKeywordDto(
            keywordId = TestKeywordId,
            userId = TestUserId,
            offsetX = TEST_OFFSET,
            offsetY = TEST_OFFSET,
            description = TEST_DESC,
        )
        private val TestCreateUserKeywordRequest = CreateUserKeywordRequest(
            userId = TestUserId.value,
            keywordId = TestKeywordId.value,
            offsetX = TEST_OFFSET,
            offsetY = TEST_OFFSET,
            description = TEST_DESC,
        )
        private val TestUserKeywordPatchDto = UserKeywordPatchDto(
            offsetX = TEST_OFFSET,
            offsetY = TEST_OFFSET,
            description = TEST_DESC,
        )
        private val TestPatchUserKeywordRequest = PatchUserKeywordRequest(
            offsetX = TEST_OFFSET,
            offsetY = TEST_OFFSET,
            description = TEST_DESC,
        )
    }
}
