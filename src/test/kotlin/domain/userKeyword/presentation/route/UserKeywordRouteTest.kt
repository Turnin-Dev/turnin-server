package com.peekr.domain.userKeyword.presentation.route

import com.peekr.common.exception.ApiException
import com.peekr.common.exception.CommonErrorCode
import com.peekr.common.jwt.JWTTestDoubles
import com.peekr.common.model.KeywordId
import com.peekr.common.model.UserId
import com.peekr.common.model.UserKeywordId
import com.peekr.common.route.Api
import com.peekr.domain.userKeyword.application.dto.CreateUserKeywordDto
import com.peekr.domain.userKeyword.application.dto.DescriptionDto
import com.peekr.domain.userKeyword.application.dto.OffsetDto
import com.peekr.domain.userKeyword.application.dto.UserKeywordDto
import com.peekr.domain.userKeyword.application.dto.toDto
import com.peekr.domain.userKeyword.application.usecase.UserKeywordUseCases
import com.peekr.domain.userKeyword.domain.model.Description
import com.peekr.domain.userKeyword.domain.model.Offset
import com.peekr.domain.userKeyword.presentation.dto.CreateUserKeywordRequest
import com.peekr.domain.userKeyword.presentation.dto.GetUserKeywordResponse
import com.peekr.domain.userKeyword.presentation.dto.UpdateDescriptionRequest
import com.peekr.domain.userKeyword.presentation.dto.UpdateOffsetRequest
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
        val response = client.get(route.ROUTE) {
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
        val response = client.get(route.ROUTE) {
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
        val response = client.get(route.ROUTE) {
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
        val response = client.get(route.ROUTE) {
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
    fun `사용자 키워드 오프셋 수정 - 요청 성공 테스트`() = testApplication {
        // given
        val route = Api.V1.UserKeyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery {
            userKeywordUseCases.updateOffset(
                ownerId = TestUserId,
                userKeywordId = TestUserKeywordId,
                patch = TestOffsetDto,
            )
        } returns TestOffsetDto

        testPlugin(
            authRouting = { userKeywordRoutes(route, userKeywordUseCases) },
        )

        // when
        val endpoint = "${route.ROUTE}${route.SAVE_OFFSET}"
        val response = client.patch(endpoint) {
            url {
                parameters.append("ownerId", TestUserId.value.toString())
                parameters.append("userKeywordId", TestUserKeywordId.value.toString())
            }
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestUpdateOffsetRequest)
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(responseBody.contains(TestUpdateOffsetRequest.offsetX.toString()))
    }

    @Test
    fun `사용자 키워드 오프셋 수정 - 수정 실패한 경우 HTTP 상태코드 404를 반환한다`() = testApplication {
        // given
        val route = Api.V1.UserKeyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery {
            userKeywordUseCases.updateOffset(
                ownerId = TestUserId,
                userKeywordId = TestUserKeywordId,
                patch = TestOffsetDto,
            )
        } returns null

        testPlugin(
            authRouting = { userKeywordRoutes(route, userKeywordUseCases) },
        )

        // when
        val endpoint = "${route.ROUTE}${route.SAVE_OFFSET}"
        val response = client.patch(endpoint) {
            url {
                parameters.append("ownerId", TestUserId.value.toString())
                parameters.append("userKeywordId", TestUserKeywordId.value.toString())
            }
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestUpdateOffsetRequest)
        }

        // then
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `사용자 키워드 오프셋 수정 - 알 수 없는 예외가 발생하는 경우 HTTP 상태코드 500을 반환한다`() = testApplication {
        // given
        val route = Api.V1.UserKeyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery {
            userKeywordUseCases.updateOffset(
                ownerId = TestUserId,
                userKeywordId = TestUserKeywordId,
                patch = TestOffsetDto,
            )
        } throws Exception()

        testPlugin(
            authRouting = { userKeywordRoutes(route, userKeywordUseCases) },
        )

        // when
        val endpoint = "${route.ROUTE}${route.SAVE_OFFSET}"
        val response = client.patch(endpoint) {
            url {
                parameters.append("ownerId", TestUserId.value.toString())
                parameters.append("userKeywordId", TestUserKeywordId.value.toString())
            }
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestUpdateOffsetRequest)
        }

        // then
        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }

    @Test
    fun `사용자 키워드 오프셋 수정 - 알려진 예외가 발생하는 경우 정해진 메시지를 반환할 수 있다`() = testApplication {
        // given
        val route = Api.V1.UserKeyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        val expectedMessage = "error!"
        val expectedException = TestApiException(expectedMessage)
        coEvery {
            userKeywordUseCases.updateOffset(
                ownerId = TestUserId,
                userKeywordId = TestUserKeywordId,
                patch = TestOffsetDto,
            )
        } throws expectedException

        testPlugin(
            authRouting = { userKeywordRoutes(route, userKeywordUseCases) },
        )

        // when
        val endpoint = "${route.ROUTE}${route.SAVE_OFFSET}"
        val response = client.patch(endpoint) {
            url {
                parameters.append("ownerId", TestUserId.value.toString())
                parameters.append("userKeywordId", TestUserKeywordId.value.toString())
            }
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestUpdateOffsetRequest)
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(expectedException.status, response.status)
        assertTrue(responseBody.contains(expectedException.errorCode.code))
    }

    @Test
    fun `사용자 키워드 설명 수정 - 요청 성공 테스트`() = testApplication {
        // given
        val route = Api.V1.UserKeyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery {
            userKeywordUseCases.updateDescription(
                ownerId = TestUserId,
                userKeywordId = TestUserKeywordId,
                patch = TestDescriptionDto,
            )
        } returns TestDescriptionDto

        testPlugin(
            authRouting = { userKeywordRoutes(route, userKeywordUseCases) },
        )

        // when
        val endpoint = "${route.ROUTE}${route.SAVE_DESCRIPTION}"
        val response = client.patch(endpoint) {
            url {
                parameters.append("ownerId", TestUserId.value.toString())
                parameters.append("userKeywordId", TestUserKeywordId.value.toString())
            }
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestUpdateDescriptionRequest)
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(HttpStatusCode.OK, response.status)
        TestUpdateDescriptionRequest.description?.let {
            assertTrue(responseBody.contains(it))
        }
    }

    @Test
    fun `사용자 키워드 설명 수정 - 수정 실패한 경우 HTTP 상태코드 404를 반환한다`() = testApplication {
        // given
        val route = Api.V1.UserKeyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery {
            userKeywordUseCases.updateDescription(
                ownerId = TestUserId,
                userKeywordId = TestUserKeywordId,
                patch = TestDescriptionDto,
            )
        } returns null

        testPlugin(
            authRouting = { userKeywordRoutes(route, userKeywordUseCases) },
        )

        // when
        val endpoint = "${route.ROUTE}${route.SAVE_DESCRIPTION}"
        val response = client.patch(endpoint) {
            url {
                parameters.append("ownerId", TestUserId.value.toString())
                parameters.append("userKeywordId", TestUserKeywordId.value.toString())
            }
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestUpdateDescriptionRequest)
        }

        // then
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `사용자 키워드 설명 수정 - 알 수 없는 예외가 발생하는 경우 HTTP 상태코드 500을 반환한다`() = testApplication {
        // given
        val route = Api.V1.UserKeyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery {
            userKeywordUseCases.updateDescription(
                ownerId = TestUserId,
                userKeywordId = TestUserKeywordId,
                patch = TestDescriptionDto,
            )
        } throws Exception()

        testPlugin(
            authRouting = { userKeywordRoutes(route, userKeywordUseCases) },
        )

        // when
        val endpoint = "${route.ROUTE}${route.SAVE_DESCRIPTION}"
        val response = client.patch(endpoint) {
            url {
                parameters.append("ownerId", TestUserId.value.toString())
                parameters.append("userKeywordId", TestUserKeywordId.value.toString())
            }
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestUpdateDescriptionRequest)
        }

        // then
        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }

    @Test
    fun `사용자 키워드 설명 수정 - 알려진 예외가 발생하는 경우 정해진 메시지를 반환할 수 있다`() = testApplication {
        // given
        val route = Api.V1.UserKeyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        val expectedMessage = "error!"
        val expectedException = TestApiException(expectedMessage)
        coEvery {
            userKeywordUseCases.updateDescription(
                ownerId = TestUserId,
                userKeywordId = TestUserKeywordId,
                patch = TestDescriptionDto,
            )
        } throws expectedException

        testPlugin(
            authRouting = { userKeywordRoutes(route, userKeywordUseCases) },
        )

        // when
        val endpoint = "${route.ROUTE}${route.SAVE_DESCRIPTION}"
        val response = client.patch(endpoint) {
            url {
                parameters.append("ownerId", TestUserId.value.toString())
                parameters.append("userKeywordId", TestUserKeywordId.value.toString())
            }
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestUpdateDescriptionRequest)
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
        private val TestOffset = Offset(10f, 10f)
        private val TestDescription = Description("hello")
        private const val TEST_KEYWORD = "keyword"
        private val TestUserKeywordDto = UserKeywordDto(
            id = TestUserKeywordId.value,
            keywordId = TestKeywordId.value,
            keywordName = TEST_KEYWORD,
            userId = TestUserId.value,
            offset = TestOffset.toDto(),
            description = TestDescription.toDto(),
            createdAt = 1000,
            updatedAt = 1000,
        )
        private val TestCreateUserKeywordDto = CreateUserKeywordDto(
            userId = TestUserId,
            keywordName = TEST_KEYWORD,
            offset = TestOffset.toDto(),
            description = TestDescription.toDto(),
        )
        private val TestCreateUserKeywordRequest = CreateUserKeywordRequest(
            userId = TestUserId.value,
            keywordName = TEST_KEYWORD,
            offsetX = TestOffset.x,
            offsetY = TestOffset.y,
            description = TestDescription.value,
        )
        private val TestOffsetDto = OffsetDto(x = TestOffset.x, y = TestOffset.y)
        private val TestUpdateOffsetRequest = UpdateOffsetRequest(offsetX = TestOffset.x, offsetY = TestOffset.y)
        private val TestDescriptionDto = DescriptionDto(value = TestDescription.value)
        private val TestUpdateDescriptionRequest = UpdateDescriptionRequest(description = TestDescription.value)
    }
}
