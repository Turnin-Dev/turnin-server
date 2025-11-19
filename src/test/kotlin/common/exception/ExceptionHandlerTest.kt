package com.peekr.common.exception

import com.peekr.common.exception.ExceptionTestDoubles.MockEmptyTestRequest
import com.peekr.common.exception.common.CommonErrorCode
import com.peekr.common.plugin.configureContentNegotiation
import com.peekr.common.validator.PeekrValidator.validation
import com.peekr.util.TestClientFactory.createTestClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.application.Application
import io.ktor.server.request.receive
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import java.io.IOException
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlinx.serialization.json.Json
import org.junit.Test

class ExceptionHandlerTest {
    @Test
    fun testApiException() = testApplication {
        application {
            configureContentNegotiation()
            configureExceptionHandler()
            fakeRouting()
        }

        val response = client.get(ApiExceptionRoute)
        val responseBody = response.bodyAsText()

        assertEquals(BadRequest, response.status)
        assertTrue(responseBody.contains("${BadRequest.value}"))
    }

    @Test
    fun testGeneralException() = testApplication {
        application {
            configureContentNegotiation()
            configureExceptionHandler()
            fakeRouting()
        }

        val response = client.get(GeneralExceptionRoute)
        val actualResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())

        assertEquals(HttpStatusCode.InternalServerError, response.status)
        assertEquals(HttpStatusCode.InternalServerError.value, actualResponse.status)
    }

    @Test
    fun `testValidationException - validation exception`() = testApplication {
        application {
            configureContentNegotiation()
            configureExceptionHandler()
            routing {
                // given
                post(ValidationExceptionRoute) {
                    val request = call.receive<TestRequest>()
                    val isNotValid = request.value.isNotEmpty()
                    validation(isNotValid) { TestErrorMessage }
                }
            }
        }

        // when
        val client = createTestClient()
        val response = client.post(ValidationExceptionRoute) {
            contentType(ContentType.Application.Json)
            setBody(MockEmptyTestRequest)
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(responseBody.contains(TestErrorMessage))
    }

    @Test
    fun `testValidationException - not validation exception`() = testApplication {
        application {
            configureContentNegotiation()
            configureExceptionHandler()
            routing {
                // given
                post(ValidationExceptionRoute) {
                    val request = call.receive<TestRequest>()
                    val isNotValid = request.value.isNotEmpty()
                    validation(isNotValid) {
                        throw NullPointerException()
                    }
                }
            }
        }

        // when
        val client = createTestClient()
        val response = client.post(ValidationExceptionRoute) {
            contentType(ContentType.Application.Json)
            setBody(MockEmptyTestRequest)
        }
        val responseBody = response.bodyAsText()

        // then
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertTrue(responseBody.contains(CommonErrorCode.ValidationDefault.description))
    }

    companion object {
        private val ApiExceptionRoute = "/api-exception-test"
        private val GeneralExceptionRoute = "/general-exception-test"
        private val ValidationExceptionRoute = "/validation-exception-test"
        private val TestErrorCode = ApiErrorCode("TestErrorCode", "TestErrorDescription")
        private val TestErrorMessage = "Test Error Message"
        private val BadRequest = HttpStatusCode.BadRequest
        private val TestApiException =
            ApiException(
                errorCode = TestErrorCode,
                message = TestErrorMessage,
                status = BadRequest,
            )
        private val TestErrorResponse =
            ErrorResponse(
                code = TestErrorCode.code,
                message = TestErrorMessage,
                status = BadRequest.value,
            )

        private fun Application.fakeRouting() {
            routing {
                get(ApiExceptionRoute) { throw TestApiException }
                get(GeneralExceptionRoute) { throw IOException(TestErrorMessage) }
            }
        }
    }
}
