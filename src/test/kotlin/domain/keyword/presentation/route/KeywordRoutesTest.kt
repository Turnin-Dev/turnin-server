package com.peekr.domain.keyword.presentation.route

import com.peekr.common.exception.ApiException
import com.peekr.common.exception.common.CommonErrorCode
import com.peekr.common.jwt.JWTTestDoubles
import com.peekr.common.model.KeywordNameValidationException
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.route.Api
import com.peekr.domain.keyword.application.dto.KeywordDto
import com.peekr.domain.keyword.application.usecase.KeywordUseCases
import com.peekr.domain.keyword.presentation.dto.CreateKeywordRequest
import com.peekr.domain.keyword.presentation.dto.KeywordResponse
import com.peekr.util.testGetEndpoint
import com.peekr.util.testPlugin
import com.peekr.util.testPostEndpoint
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.Test

private class TestApiException(message: String) :
    ApiException(
        errorCode = CommonErrorCode.MalformedRequest,
        status = HttpStatusCode.InternalServerError,
        message = message,
    )

class KeywordRoutesTest {
    private val keywordUseCases: KeywordUseCases = mockk()
    private val route = Api.V1.Keyword

    @Test
    fun `키워드 ID로 키워드 조회 - 요청 성공 테스트`() = testApplication {
        coEvery { keywordUseCases.get(TestKeywordId) } returns TestKeywordDto

        testGetEndpoint(
            endpoint = "${route.ROUTE}/${route.ID}/${TestKeywordId.value}",
            queryParameters = null,
            testPlugin = {
                testPlugin(
                    authRouting = { keywordRoutes(route, keywordUseCases) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
            additionalAssertions = {
                coVerify(exactly = 1) { keywordUseCases.get(TestKeywordId) }
            },
            responseValidator = {
                contains(TestKeywordResponse.keyword)
            },
        )
    }

    @Test
    fun `키워드 ID로 키워드 조회 - 키워드가 존재하지 않을 때 HTTP 상태코드 NotFound를 반환한다`() = testApplication {
        coEvery { keywordUseCases.get(TestKeywordId) } returns null

        testGetEndpoint(
            endpoint = "${route.ROUTE}/${route.ID}/${TestKeywordId.value}",
            testPlugin = {
                testPlugin(
                    authRouting = { keywordRoutes(route, keywordUseCases) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.NotFound,
            additionalAssertions = {
                coVerify(exactly = 1) { keywordUseCases.get(TestKeywordId) }
            },
        )
    }

    @Test
    fun `키워드 ID로 키워드 조회 - 키워드 ID 유효성 검사 실패 테스트`() = testApplication {
        coEvery { keywordUseCases.get(TestKeywordId) } returns TestKeywordDto

        testGetEndpoint(
            endpoint = "${route.ROUTE}/${route.ID}/-1",
            testPlugin = {
                testPlugin(
                    authRouting = { keywordRoutes(route, keywordUseCases) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.BadRequest,
        )
    }

    @Test
    fun `키워드 ID로 키워드 조회 - 알 수 없는 예외가 발생하는 경우 HTTP 상태코드 500을 반환한다`() = testApplication {
        coEvery { keywordUseCases.get(TestKeywordId) } throws Exception("")

        testGetEndpoint(
            endpoint = "${route.ROUTE}/${route.ID}/${TestKeywordId.value}",
            testPlugin = {
                testPlugin(
                    authRouting = { keywordRoutes(route, keywordUseCases) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.InternalServerError,
        )
    }

    @Test
    fun `키워드 ID로 키워드 조회 - 알려진 예외가 발생하는 경우 정해진 메시지를 반환할 수 있다`() = testApplication {
        val expectedMessage = "expected message"
        val expectedException = TestApiException(expectedMessage)
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery { keywordUseCases.get(TestKeywordId) } throws expectedException

        testGetEndpoint(
            endpoint = "${route.ROUTE}/${route.ID}/${TestKeywordId.value}",
            testPlugin = {
                testPlugin(
                    authRouting = { keywordRoutes(route, keywordUseCases) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = expectedException.status,
            responseValidator = {
                contains(expectedException.errorCode.code)
            },
        )
    }

    @Test
    fun `키워드 생성 - 요청 성공 테스트`() = testApplication {
        coEvery {
            keywordUseCases.create(TEST_KEYWORD, TestUserId)
        } returns TestKeywordDto

        testPostEndpoint(
            endpoint = route.ROUTE,
            requestBody = TestCreateKeywordRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { keywordRoutes(route, keywordUseCases) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.Created,
            additionalAssertions = {
                coVerify(exactly = 1) { keywordUseCases.create(TEST_KEYWORD, TestUserId) }
            },
            responseValidator = {
                contains(TestCreateKeywordRequest.keyword)
            },
        )
    }

    @Test
    fun `키워드 생성 - 토큰 에러 발생 시 HTTP 상태코드 401을 반환한다`() = testApplication {
        coEvery {
            keywordUseCases.create(TEST_KEYWORD, TestUserId)
        } returns TestKeywordDto

        testPostEndpoint(
            endpoint = route.ROUTE,
            requestBody = TestCreateKeywordRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { keywordRoutes(route, keywordUseCases) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
            additionalAssertions = {
                coVerify(exactly = 0) { keywordUseCases.create(TEST_KEYWORD, TestUserId) }
            },
        )
    }

    @Test
    fun `키워드 생성 - 알 수 없는 예외가 발생하는 경우 HTTP 상태코드 500을 반환한다`() = testApplication {
        coEvery {
            keywordUseCases.create(TEST_KEYWORD, TestUserId)
        } throws Exception()

        testPostEndpoint(
            endpoint = route.ROUTE,
            requestBody = TestCreateKeywordRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { keywordRoutes(route, keywordUseCases) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.InternalServerError,
        )
    }

    @Test
    fun `키워드 생성 - 알려진 예외가 발생하는 경우 정해진 메시지를 반환할 수 있다`() = testApplication {
        val expectedMessage = "expected message"
        val expectedException = TestApiException(expectedMessage)
        coEvery {
            keywordUseCases.create(TEST_KEYWORD, TestUserId)
        } throws expectedException

        testPostEndpoint(
            endpoint = route.ROUTE,
            requestBody = TestCreateKeywordRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { keywordRoutes(route, keywordUseCases) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = expectedException.status,
            responseValidator = {
                contains(expectedException.errorCode.code)
            },
        )
    }

    @Test
    fun `키워드 명으로 키워드 조회 - 요청 성공 테스트`() = testApplication {
        coEvery { keywordUseCases.getByName(TEST_KEYWORD) } returns TestKeywordDto

        testGetEndpoint(
            endpoint = "${route.ROUTE}/${route.NAME}/$TEST_KEYWORD",
            testPlugin = {
                testPlugin(
                    authRouting = { keywordRoutes(route, keywordUseCases) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
            additionalAssertions = {
                coVerify(exactly = 1) { keywordUseCases.getByName(TEST_KEYWORD) }
            },
            responseValidator = {
                contains(TestKeywordResponse.keyword)
            },
        )
    }

    @Test
    fun `키워드 명으로 키워드 조회 - 키워드가 존재하지 않을 때 HTTP 상태코드 NotFound를 반환한다`() = testApplication {
        coEvery { keywordUseCases.getByName(TEST_KEYWORD) } returns null

        testGetEndpoint(
            endpoint = "${route.ROUTE}/${route.NAME}/$TEST_KEYWORD",
            testPlugin = {
                testPlugin(
                    authRouting = { keywordRoutes(route, keywordUseCases) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.NotFound,
            additionalAssertions = {
                coVerify(exactly = 1) { keywordUseCases.getByName(TEST_KEYWORD) }
            },
        )
    }

    @Test
    fun `키워드 명으로 키워드 조회 - 키워드 명 유효성 검사 실패 테스트`() = testApplication {
        val expectedErrorMessage = "invalid keyword name"
        coEvery {
            keywordUseCases.getByName(any())
        } throws KeywordNameValidationException(expectedErrorMessage)

        testGetEndpoint(
            endpoint = "${route.ROUTE}/${route.NAME}/lkj",
            testPlugin = {
                testPlugin(
                    authRouting = { keywordRoutes(route, keywordUseCases) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.BadRequest,
            responseValidator = {
                contains(expectedErrorMessage)
            },
        )
    }

    @Test
    fun `키워드 명으로 키워드 조회 - 알 수 없는 예외가 발생하는 경우 HTTP 상태코드 500을 반환한다`() = testApplication {
        coEvery {
            keywordUseCases.getByName(TEST_KEYWORD)
        } throws Exception("")

        testGetEndpoint(
            endpoint = "${route.ROUTE}/${route.NAME}/$TEST_KEYWORD",
            testPlugin = {
                testPlugin(
                    authRouting = { keywordRoutes(route, keywordUseCases) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.InternalServerError,
        )
    }

    @Test
    fun `키워드 명으로 키워드 조회 - 알려진 예외가 발생하는 경우 정해진 메시지를 반환할 수 있다`() = testApplication {
        val expectedMessage = "expected message"
        val expectedException = TestApiException(expectedMessage)
        val token = JWTTestDoubles.getMockJWTToken(TestUserId.value.toString())
        coEvery {
            keywordUseCases.getByName(TEST_KEYWORD)
        } throws expectedException

        testGetEndpoint(
            endpoint = "${route.ROUTE}/${route.NAME}/$TEST_KEYWORD",
            testPlugin = {
                testPlugin(
                    authRouting = { keywordRoutes(route, keywordUseCases) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = expectedException.status,
            responseValidator = {
                contains(expectedException.errorCode.code)
            },
        )
    }

    companion object {
        private val TestKeywordId = KeywordId(1)
        private val TestUserId = UserId(1)
        private const val TEST_KEYWORD = "keyword"
        private val TestKeywordDto = KeywordDto(
            id = TestKeywordId,
            name = TEST_KEYWORD,
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
