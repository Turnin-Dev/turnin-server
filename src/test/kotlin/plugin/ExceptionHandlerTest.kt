package com.peekr.plugin

import com.peekr.exception.ApiException
import com.peekr.exception.ErrorResponse
import com.peekr.util.testPlugin
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals

class ExceptionHandlerTest {

    @Test
    fun testApiException() = testApplication {
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
    fun testGeneralException() = testApplication {
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
        private val TestApiException = ApiException(
            code = TestCode,
            message = TestMessage,
            status = TestStatus
        )
        private val TestErrorResponse = ErrorResponse(
            code = TestCode,
            message = TestMessage,
            status = TestStatus.value
        )
    }
}