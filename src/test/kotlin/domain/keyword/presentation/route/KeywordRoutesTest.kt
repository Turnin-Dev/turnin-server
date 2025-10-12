package com.peekr.domain.keyword.presentation.route

import com.peekr.common.exception.ApiException
import com.peekr.common.exception.CommonErrorCode
import com.peekr.common.jwt.JWTTestDoubles
import com.peekr.common.model.KeywordId
import com.peekr.common.model.UserId
import com.peekr.common.route.Api
import com.peekr.domain.keyword.application.dto.KeywordDto
import com.peekr.domain.keyword.application.usecase.KeywordUseCases
import com.peekr.domain.keyword.domain.model.Keyword
import com.peekr.domain.keyword.presentation.dto.CreateKeywordRequest
import com.peekr.domain.keyword.presentation.dto.KeywordResponse
import com.peekr.util.TestClientFactory.createTestClient
import com.peekr.util.testPlugin
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class TestApiException(message: String) :
    ApiException(
        errorCode = CommonErrorCode.MalformedRequest,
        status = HttpStatusCode.InternalServerError,
        message = message,
    )

class KeywordRoutesTest {
    private val keywordUseCases: KeywordUseCases = mockk()

    @Test
    fun `키워드 ID로 키워드 조회 - 요청 성공 테스트`() = testApplication {
        // given
        val route = Api.V1.Keyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery { keywordUseCases.get(TestKeywordId) } returns TestKeywordDto

        testPlugin(
            authRouting = { keywordRoutes(route, keywordUseCases) },
        )

        // when
        val endpoint = "${route.ROUTE}/${route.ID}/${TestKeywordId.value}"
        val response = client.get(endpoint) {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestKeywordResponse)
        }
        val responseBody = response.bodyAsText()

        // then
        coVerify(exactly = 1) { keywordUseCases.get(TestKeywordId) }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(responseBody.contains(TestKeywordResponse.keyword))
    }

    @Test
    fun `키워드 ID로 키워드 조회 - 키워드가 존재하지 않을 때 HTTP 상태코드 NotFound를 반환한다`() = testApplication {
        // given
        val route = Api.V1.Keyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery { keywordUseCases.get(TestKeywordId) } returns null

        testPlugin(
            authRouting = { keywordRoutes(route, keywordUseCases) },
        )

        // when
        val endpoint = "${route.ROUTE}/${route.ID}/${TestKeywordId.value}"
        val response = client.get(endpoint) {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestKeywordResponse)
        }

        // then
        coVerify(exactly = 1) { keywordUseCases.get(TestKeywordId) }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `키워드 ID로 키워드 조회 - 키워드 ID 유효성 검사 실패 테스트`() = testApplication {
        // given
        val route = Api.V1.Keyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery { keywordUseCases.get(TestKeywordId) } returns TestKeywordDto

        testPlugin(
            authRouting = { keywordRoutes(route, keywordUseCases) },
        )

        // when
        val endpoint = "${route.ROUTE}/${route.ID}/-1"
        val response = client.get(endpoint) {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestKeywordResponse)
        }

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `키워드 ID로 키워드 조회 - 알 수 없는 예외가 발생하는 경우 HTTP 상태코드 500을 반환한다`() = testApplication {
        // given
        val route = Api.V1.Keyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery {
            keywordUseCases.get(TestKeywordId)
        } throws Exception("")

        testPlugin(
            authRouting = { keywordRoutes(route, keywordUseCases) },
        )

        // when
        val endpoint = "${route.ROUTE}/${route.ID}/${TestKeywordId.value}"
        val response = client.get(endpoint) {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestKeywordResponse)
        }

        // then
        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }

    @Test
    fun `키워드 ID로 키워드 조회 - 알려진 예외가 발생하는 경우 정해진 메시지를 반환할 수 있다`() = testApplication {
        // given
        val route = Api.V1.Keyword
        val client = createTestClient()
        val expectedMessage = "expected message"
        val expectedException = TestApiException(expectedMessage)
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery {
            keywordUseCases.get(TestKeywordId)
        } throws expectedException

        testPlugin(
            authRouting = { keywordRoutes(route, keywordUseCases) },
        )

        // when
        val endpoint = "${route.ROUTE}/${route.ID}/${TestKeywordId.value}"
        val response = client.get(endpoint) {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestKeywordResponse)
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(expectedException.status, response.status)
        assertTrue(responseBody.contains(expectedException.errorCode.code))
    }

    @Test
    fun `키워드 생성 - 요청 성공 테스트`() = testApplication {
        // given
        val route = Api.V1.Keyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())

        coEvery {
            keywordUseCases.create(TEST_KEYWORD, TestUserId)
        } returns TestKeywordDto

        testPlugin(
            authRouting = { keywordRoutes(route, keywordUseCases) },
        )

        // when
        val endpoint = route.ROUTE
        val response = client.post(endpoint) {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestCreateKeywordRequest)
        }
        val responseBody = response.bodyAsText()

        // then
        coVerify(exactly = 1) { keywordUseCases.create(TEST_KEYWORD, TestUserId) }
        assertEquals(HttpStatusCode.Created, response.status)
        assertTrue(responseBody.contains(TestCreateKeywordRequest.keyword))
    }

    @Test
    fun `키워드 생성 - 토큰 에러 발생 시 HTTP 상태코드 401을 반환한다`() = testApplication {
        // given
        val route = Api.V1.Keyword
        val client = createTestClient()

        coEvery {
            keywordUseCases.create(TEST_KEYWORD, TestUserId)
        } returns TestKeywordDto

        testPlugin(
            authRouting = { keywordRoutes(route, keywordUseCases) },
        )

        // when
        val endpoint = route.ROUTE
        val response = client.post(endpoint) {
            contentType(ContentType.Application.Json)
            setBody(TestCreateKeywordRequest)
        }

        // then
        coVerify(exactly = 0) { keywordUseCases.create(TEST_KEYWORD, TestUserId) }
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun `키워드 생성 - 알 수 없는 예외가 발생하는 경우 HTTP 상태코드 500을 반환한다`() = testApplication {
        // given
        val route = Api.V1.Keyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery {
            keywordUseCases.create(TEST_KEYWORD, TestUserId)
        } throws Exception()

        testPlugin(
            authRouting = { keywordRoutes(route, keywordUseCases) },
        )

        // when
        val endpoint = route.ROUTE
        val response = client.post(endpoint) {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestCreateKeywordRequest)
        }

        // then
        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }

    @Test
    fun `키워드 생성 - 알려진 예외가 발생하는 경우 정해진 메시지를 반환할 수 있다`() = testApplication {
        // given
        val route = Api.V1.Keyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        val expectedMessage = "expected message"
        val expectedException = TestApiException(expectedMessage)
        coEvery {
            keywordUseCases.create(TEST_KEYWORD, TestUserId)
        } throws expectedException

        testPlugin(
            authRouting = { keywordRoutes(route, keywordUseCases) },
        )

        // when
        val endpoint = route.ROUTE
        val response = client.post(endpoint) {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestCreateKeywordRequest)
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(expectedException.status, response.status)
        assertTrue(responseBody.contains(expectedException.errorCode.code))
    }

    @Test
    fun `키워드 명으로 키워드 조회 - 요청 성공 테스트`() = testApplication {
        // given
        val route = Api.V1.Keyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery { keywordUseCases.getByName(TEST_KEYWORD) } returns TestKeywordDto

        testPlugin(
            authRouting = { keywordRoutes(route, keywordUseCases) },
        )

        // when
        val endpoint = "${route.ROUTE}/${route.NAME}/$TEST_KEYWORD"
        val response = client.get(endpoint) {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestCreateKeywordRequest)
        }
        val responseBody = response.bodyAsText()

        // then
        coVerify(exactly = 1) { keywordUseCases.getByName(TEST_KEYWORD) }
        assertEquals(HttpStatusCode.OK, response.status)
        assertTrue(responseBody.contains(TestKeywordResponse.keyword))
    }

    @Test
    fun `키워드 명으로 키워드 조회 - 키워드가 존재하지 않을 때 HTTP 상태코드 NotFound를 반환한다`() = testApplication {
        // given
        val route = Api.V1.Keyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery { keywordUseCases.getByName(TEST_KEYWORD) } returns null

        testPlugin(
            authRouting = { keywordRoutes(route, keywordUseCases) },
        )

        // when
        val endpoint = "${route.ROUTE}/${route.NAME}/$TEST_KEYWORD"
        val response = client.get(endpoint) {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestKeywordResponse)
        }

        // then
        coVerify(exactly = 1) { keywordUseCases.getByName(TEST_KEYWORD) }
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun `키워드 명으로 키워드 조회 - 키워드 명 유효성 검사 실패 테스트`() = testApplication {
        // given
        val route = Api.V1.Keyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery { keywordUseCases.getByName(TEST_KEYWORD) } returns TestKeywordDto

        testPlugin(
            authRouting = { keywordRoutes(route, keywordUseCases) },
        )

        // when
        val endpoint = "${route.ROUTE}/${route.NAME}/${"a".repeat(Keyword.MAX_LENGTH + 1)}"
        val response = client.get(endpoint) {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestKeywordResponse)
        }

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun `키워드 명으로 키워드 조회 - 알 수 없는 예외가 발생하는 경우 HTTP 상태코드 500을 반환한다`() = testApplication {
        // given
        val route = Api.V1.Keyword
        val client = createTestClient()
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery {
            keywordUseCases.getByName(TEST_KEYWORD)
        } throws Exception("")

        testPlugin(
            authRouting = { keywordRoutes(route, keywordUseCases) },
        )

        // when
        val endpoint = "${route.ROUTE}/${route.NAME}/$TEST_KEYWORD"
        val response = client.get(endpoint) {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestKeywordResponse)
        }

        // then
        assertEquals(HttpStatusCode.InternalServerError, response.status)
    }

    @Test
    fun `키워드 명으로 키워드 조회 - 알려진 예외가 발생하는 경우 정해진 메시지를 반환할 수 있다`() = testApplication {
        // given
        val route = Api.V1.Keyword
        val client = createTestClient()
        val expectedMessage = "expected message"
        val expectedException = TestApiException(expectedMessage)
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery {
            keywordUseCases.getByName(TEST_KEYWORD)
        } throws expectedException

        testPlugin(
            authRouting = { keywordRoutes(route, keywordUseCases) },
        )

        // when
        val endpoint = "${route.ROUTE}/${route.NAME}/$TEST_KEYWORD"
        val response = client.get(endpoint) {
            header(HttpHeaders.Authorization, "Bearer ${token.accessToken}")
            contentType(ContentType.Application.Json)
            setBody(TestKeywordResponse)
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(expectedException.status, response.status)
        assertTrue(responseBody.contains(expectedException.errorCode.code))
    }

    companion object {
        private val TestKeywordId = KeywordId(1)
        private val TestUserId = UserId(1)
        private const val TEST_KEYWORD = "keyword"
        private val TestKeywordDto = KeywordDto(
            id = TestKeywordId,
            keyword = TEST_KEYWORD,
            createdBy = TestUserId,
            createdAt = 1000,
            updatedAt = 1000,
        )
        private val TestKeywordResponse = KeywordResponse(
            id = TestKeywordId.value,
            keyword = TEST_KEYWORD,
            createdBy = TestUserId.value,
            createdAt = 1000,
            updatedAt = 1000,
        )
        private val TestCreateKeywordRequest = CreateKeywordRequest(TEST_KEYWORD)
    }
}
