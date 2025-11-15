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

object TestEndpoint {
    suspend fun ApplicationTestBuilder.getEndpointTest(
        tokenSubject: String?,
        testPlugin: ApplicationTestBuilder.() -> Unit,
        endpoint: String,
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

    suspend fun ApplicationTestBuilder.patchEndpointTest(
        tokenSubject: String?,
        testPlugin: ApplicationTestBuilder.() -> Unit,
        endpoint: String,
        requestBuilder: HttpRequestBuilder.() -> Unit,
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
