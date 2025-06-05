package com.peekr.plugin

import com.peekr.common.exception.ApiException
import com.peekr.common.exception.ErrorResponse
import com.peekr.common.exception.configureExceptionHandler
import com.peekr.util.testPlugin
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.testApplication
import kotlin.test.assertEquals
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import org.junit.Test

class ExceptionHandlerTest {
    @Test
    fun testApiException() =
        testApplication {
            application {
                testPlugin()
                configureExceptionHandler()
                fakeRouting()
            }

            val response = client.get(ApiExceptionRoute)
            val actualResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())

            assertEquals(TestStatus, response.status)
            assertEquals(TestErrorResponse, actualResponse)
        }

    @Test
    fun testGeneralException() =
        testApplication {
            application {
                testPlugin()
                configureExceptionHandler()
                fakeRouting()
            }

            val response = client.get(GeneralExceptionRoute)
            val actualResponse = Json.decodeFromString<ErrorResponse>(response.bodyAsText())

            assertEquals(HttpStatusCode.InternalServerError, response.status)
            assertEquals(HttpStatusCode.InternalServerError.value, actualResponse.status)
        }

    private fun Application.fakeRouting() {
        routing {
            get(ApiExceptionRoute) { throw TestApiException }
            get(GeneralExceptionRoute) { throw IOException(TestMessage) }
        }
    }

    companion object {
        private val ApiExceptionRoute = "/api-exception-test"
        private val GeneralExceptionRoute = "/general-exception-test"
        private val TestCode = "Test Code"
        private val TestMessage = "Test Message"
        private val TestStatus = HttpStatusCode.BadRequest
        private val TestApiException =
            ApiException(
                code = TestCode,
                message = TestMessage,
                status = TestStatus,
            )
        private val TestErrorResponse =
            ErrorResponse(
                code = TestCode,
                message = TestMessage,
                status = TestStatus.value,
            )
    }
}
