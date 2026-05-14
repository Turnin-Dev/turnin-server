package com.turnin.util

import com.turnin.common.jwt.JWTTestDoubles
import com.turnin.util.TestClientFactory.createTestClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** 응답 유효성 검사 */
class ResponseValidator(
    private val body: String,
    private val response: HttpResponse,
) {
    /**
     * 응답 바디에 특정 문자열이 포함되어 있는지 검증
     *
     * @param expected 예상되는 문자열
     * @param message 실패 시 문자열
     */
    fun contains(expected: String, message: String? = null) {
        assertTrue(
            actual = body.contains(expected),
            message = message ?: "Response body does not contain $expected",
        )
    }

    /**
     * 응답 바디에 여러 문자열이 모두 포함되어 있는지 검증
     */
    fun containsAll(vararg expected: String) {
        expected.forEach { contains(it) }
    }

    /**
     * 응답 바디가 비어있는지 검증
     */
    fun isEmpty() {
        assertTrue(body.isEmpty())
    }

    /**
     * 커스텀 검증 로직
     */
    fun custom(assertion: (body: String, response: HttpResponse) -> Unit) {
        assertion(body, response)
    }
}

/**
 * 엔드포인트 테스트 도구
 *
 * @param method HTTP 메서드
 * @param endpoint 테스트할 엔드포인트
 * @param queryParameters 쿼리 파라미터를 [Map]형태로 입력한다.
 * @param requestBody 요청 바디
 * @param requestBuilder 엔드포인트 요청 시 추가 요청 블록
 * @param testPlugin 테스트 플러그인 [testPlugin]을 사용한다.
 * @param tokenSubject 인증 토큰 Subject
 * @param expectedStatus 예상되는 HTTP 상태 코드
 * @param additionalAssertions 추가 검증 블록
 * @param responseValidator 응답 바디 검증
 */
private suspend fun ApplicationTestBuilder.testEndpoint(
    method: HttpMethod,
    endpoint: String,
    queryParameters: Map<String, String>?,
    requestBody: Any? = null,
    requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    testPlugin: ApplicationTestBuilder.() -> Unit,
    tokenSubject: String?,
    expectedStatus: HttpStatusCode,
    additionalAssertions: (() -> Unit)?,
    responseValidator: ResponseValidator.() -> Unit = {},
) {
    // given
    val client = createTestClient()
    val token = tokenSubject?.let {
        JWTTestDoubles.getMockJWTToken(it)
    }
    testPlugin()

    // when
    val response = client.request(endpoint) {
        this.method = method

        // 쿼리 파라미터
        url {
            queryParameters?.forEach { (key, value) ->
                parameters.append(key, value)
            }
        }

        // 인증 헤더
        token?.let {
            header(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
        }

        // 요청 바디
        if (requestBody != null) {
            contentType(ContentType.Application.Json)
            setBody(requestBody)
        }

        // 추가 요청 설정
        requestBuilder?.invoke(this)
    }

    // then
    val responseBody = response.bodyAsText()
    assertEquals(expectedStatus, response.status, "HTTP status code mismatch")
    additionalAssertions?.invoke()

    // 응답 바디 검증
    ResponseValidator(responseBody, response).apply(responseValidator)
}

/**
 * GET 엔드포인트 테스트
 *
 * @param endpoint 테스트할 엔드포인트
 * @param queryParameters 쿼리 파라미터를 [Map]형태로 입력한다.
 * @param requestBuilder 엔드포인트 요청 시 추가 요청 블록
 * @param testPlugin 테스트 플러그인 [testPlugin]을 사용한다.
 * @param tokenSubject 인증 토큰 Subject
 * @param expectedStatus 예상되는 HTTP 상태 코드
 * @param additionalAssertions 추가 검증 블록
 * @param responseValidator 응답 바디 검증
 */
suspend fun ApplicationTestBuilder.testGetEndpoint(
    endpoint: String,
    queryParameters: Map<String, String>? = null,
    requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    testPlugin: ApplicationTestBuilder.() -> Unit,
    tokenSubject: String? = null,
    expectedStatus: HttpStatusCode,
    additionalAssertions: (() -> Unit)? = null,
    responseValidator: ResponseValidator.() -> Unit = {},
) = testEndpoint(
    method = HttpMethod.Get,
    endpoint = endpoint,
    queryParameters = queryParameters,
    requestBuilder = requestBuilder,
    testPlugin = testPlugin,
    tokenSubject = tokenSubject,
    expectedStatus = expectedStatus,
    additionalAssertions = additionalAssertions,
    responseValidator = responseValidator,
)

/**
 * POST 엔드포인트 테스트
 *
 * @param endpoint 테스트할 엔드포인트
 * @param queryParameters 쿼리 파라미터를 [Map]형태로 입력한다.
 * @param requestBody 요청 바디
 * @param requestBuilder 엔드포인트 요청 시 추가 요청 블록
 * @param testPlugin 테스트 플러그인 [testPlugin]을 사용한다.
 * @param tokenSubject 인증 토큰 Subject
 * @param expectedStatus 예상되는 HTTP 상태 코드
 * @param additionalAssertions 추가 검증 블록
 * @param responseValidator 응답 바디 검증
 */
suspend fun ApplicationTestBuilder.testPostEndpoint(
    endpoint: String,
    queryParameters: Map<String, String>? = null,
    requestBody: Any? = null,
    requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    testPlugin: ApplicationTestBuilder.() -> Unit,
    tokenSubject: String? = null,
    expectedStatus: HttpStatusCode,
    additionalAssertions: (() -> Unit)? = null,
    responseValidator: ResponseValidator.() -> Unit = {},
) = testEndpoint(
    method = HttpMethod.Post,
    endpoint = endpoint,
    queryParameters = queryParameters,
    requestBody = requestBody,
    requestBuilder = requestBuilder,
    testPlugin = testPlugin,
    tokenSubject = tokenSubject,
    expectedStatus = expectedStatus,
    additionalAssertions = additionalAssertions,
    responseValidator = responseValidator,
)

/**
 * PATCH 엔드포인트 테스트
 *
 * @param endpoint 테스트할 엔드포인트
 * @param queryParameters 쿼리 파라미터를 [Map]형태로 입력한다.
 * @param requestBody 요청 바디
 * @param requestBuilder 엔드포인트 요청 시 추가 요청 블록
 * @param testPlugin 테스트 플러그인 [testPlugin]을 사용한다.
 * @param tokenSubject 인증 토큰 Subject
 * @param expectedStatus 예상되는 HTTP 상태 코드
 * @param additionalAssertions 추가 검증 블록
 * @param responseValidator 응답 바디 검증
 */
suspend fun ApplicationTestBuilder.testPatchEndpoint(
    endpoint: String,
    queryParameters: Map<String, String>? = null,
    requestBody: Any? = null,
    requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    testPlugin: ApplicationTestBuilder.() -> Unit,
    tokenSubject: String? = null,
    expectedStatus: HttpStatusCode,
    additionalAssertions: (() -> Unit)? = null,
    responseValidator: ResponseValidator.() -> Unit = {},
) = testEndpoint(
    method = HttpMethod.Patch,
    endpoint = endpoint,
    queryParameters = queryParameters,
    requestBody = requestBody,
    requestBuilder = requestBuilder,
    testPlugin = testPlugin,
    tokenSubject = tokenSubject,
    expectedStatus = expectedStatus,
    additionalAssertions = additionalAssertions,
    responseValidator = responseValidator,
)

/**
 * PUT 엔드포인트 테스트
 *
 * @param endpoint 테스트할 엔드포인트
 * @param queryParameters 쿼리 파라미터를 [Map]형태로 입력한다.
 * @param requestBody 요청 바디
 * @param requestBuilder 엔드포인트 요청 시 추가 요청 블록
 * @param testPlugin 테스트 플러그인 [testPlugin]을 사용한다.
 * @param tokenSubject 인증 토큰 Subject
 * @param expectedStatus 예상되는 HTTP 상태 코드
 * @param additionalAssertions 추가 검증 블록
 * @param responseValidator 응답 바디 검증
 */
suspend fun ApplicationTestBuilder.testPutEndpoint(
    endpoint: String,
    queryParameters: Map<String, String>? = null,
    requestBody: Any? = null,
    requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    testPlugin: ApplicationTestBuilder.() -> Unit,
    tokenSubject: String? = null,
    expectedStatus: HttpStatusCode,
    additionalAssertions: (() -> Unit)? = null,
    responseValidator: ResponseValidator.() -> Unit = {},
) = testEndpoint(
    method = HttpMethod.Put,
    endpoint = endpoint,
    queryParameters = queryParameters,
    requestBody = requestBody,
    requestBuilder = requestBuilder,
    testPlugin = testPlugin,
    tokenSubject = tokenSubject,
    expectedStatus = expectedStatus,
    additionalAssertions = additionalAssertions,
    responseValidator = responseValidator,
)

/**
 * DELETE 엔드포인트 테스트
 *
 * @param endpoint 테스트할 엔드포인트
 * @param queryParameters 쿼리 파라미터를 [Map]형태로 입력한다.
 * @param requestBody 요청 바디
 * @param requestBuilder 엔드포인트 요청 시 추가 요청 블록
 * @param testPlugin 테스트 플러그인 [testPlugin]을 사용한다.
 * @param tokenSubject 인증 토큰 Subject
 * @param expectedStatus 예상되는 HTTP 상태 코드
 * @param additionalAssertions 추가 검증 블록
 * @param responseValidator 응답 바디 검증
 */
suspend fun ApplicationTestBuilder.testDeleteEndpoint(
    endpoint: String,
    queryParameters: Map<String, String>? = null,
    requestBody: Any? = null,
    requestBuilder: (HttpRequestBuilder.() -> Unit)? = null,
    testPlugin: ApplicationTestBuilder.() -> Unit,
    tokenSubject: String? = null,
    expectedStatus: HttpStatusCode,
    additionalAssertions: (() -> Unit)? = null,
    responseValidator: ResponseValidator.() -> Unit = {},
) = testEndpoint(
    method = HttpMethod.Delete,
    endpoint = endpoint,
    queryParameters = queryParameters,
    requestBody = requestBody,
    requestBuilder = requestBuilder,
    testPlugin = testPlugin,
    tokenSubject = tokenSubject,
    expectedStatus = expectedStatus,
    additionalAssertions = additionalAssertions,
    responseValidator = responseValidator,
)
