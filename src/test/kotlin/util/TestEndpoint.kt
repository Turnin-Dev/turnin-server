package com.peekr.util

import com.peekr.common.jwt.JWTTestDoubles
import com.peekr.util.TestClientFactory.createTestClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.patch
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * API 엔드포인트 테스트 도구
 */
object TestEndpoint {
    /**
     * GET 엔드포인트를 테스트한다.
     *
     * ##### Usage
     * ```
     *         val route = Api.V1.User
     *         coEvery { userUseCases.get(TestUserId) } returns MockUserDto
     *
     *         getEndpointTest(
     *             endpoint = route.ROUTE,
     *             queryParameters = mapOf("id" to "1"),
     *             testPlugin = {
     *                 testPlugin(
     *                     authRouting = { userRoutes(route, userUseCases) },
     *                 )
     *             },
     *             tokenSubject = TestUserId.value.toString(),
     *             expectedHttpStatusCode = HttpStatusCode.OK,
     *             // expectedResponseBodies ->
     *             MockUserDto.name.value,
     *             MockUserDto.displayId.value,
     *             MockUserDto.role.name,
     *         )
     * ```
     *
     * @param endpoint 테스트할 엔드포인트
     * @param queryParameters 쿼리 파라미터를 [Map]형태로 입력한다.
     * @param testPlugin 테스트 플러그인 [testPlugin]을 사용한다.
     * @param tokenSubject 인증 토큰 Subject
     * @param expectedHttpStatusCode 예상되는 HTTP 상태 코드
     * @param expectedResponseBody 예상되는 응답 바디
     */
    suspend fun ApplicationTestBuilder.testGetEndpoint(
        endpoint: String,
        queryParameters: Map<String, String>?,
        testPlugin: ApplicationTestBuilder.() -> Unit,
        tokenSubject: String?,
        expectedHttpStatusCode: HttpStatusCode,
        vararg expectedResponseBody: String,
    ) {
        // given
        val client = createTestClient()
        val token = tokenSubject?.let {
            JWTTestDoubles.getMockJWTToken(it)
        }
        testPlugin()

        // when
        val response = client.get(endpoint) {
            url {
                queryParameters?.let {
                    it.forEach { (key, value) -> parameters.append(key, value) }
                }
            }
            token?.let {
                header(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
            }
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(expectedHttpStatusCode, response.status)
        expectedResponseBody.forEach {
            assertTrue(responseBody.contains(it))
        }
    }

    /**
     * PATCH 엔드포인트를 테스트한다.
     *
     * @param endpoint 테스트할 엔드포인트
     * @param queryParameters 쿼리 파라미터를 [Map]형태로 입력한다.
     * @param requestBuilder 요청 옵션 ([HttpRequestBuilder] 내부에서 사용 가능한 옵션은 전부 다 가능하다)
     * @param testPlugin 테스트 플러그인 [testPlugin]을 사용한다.
     * @param tokenSubject 인증 토큰 Subject
     * @param expectedHttpStatusCode 예상되는 HTTP 상태 코드
     * @param expectedResponseBody 예상되는 응답 바디
     */
    suspend fun ApplicationTestBuilder.testPatchEndpoint(
        endpoint: String,
        queryParameters: Map<String, String>?,
        requestBuilder: HttpRequestBuilder.() -> Unit,
        testPlugin: ApplicationTestBuilder.() -> Unit,
        tokenSubject: String?,
        expectedHttpStatusCode: HttpStatusCode,
        vararg expectedResponseBody: String,
    ) {
        // given
        val client = createTestClient()
        val token = tokenSubject?.let {
            JWTTestDoubles.getMockJWTToken(it)
        }
        testPlugin()

        // when
        val response = client.patch(endpoint) {
            url {
                queryParameters?.let {
                    it.forEach { (key, value) -> parameters.append(key, value) }
                }
            }
            token?.let {
                header(HttpHeaders.Authorization, "Bearer ${it.accessToken}")
            }
            contentType(ContentType.Application.Json)
            requestBuilder()
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(expectedHttpStatusCode, response.status)
        expectedResponseBody.forEach {
            assertTrue(responseBody.contains(it))
        }
    }
}
