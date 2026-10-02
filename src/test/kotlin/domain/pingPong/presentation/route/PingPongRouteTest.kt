package com.turnin.domain.pingPong.presentation.route

import com.turnin.common.exception.ApiException
import com.turnin.common.exception.common.CommonErrorCode
import com.turnin.common.model.id.UserId
import com.turnin.common.route.Api
import com.turnin.domain.pingPong.application.dto.PingPongDto
import com.turnin.domain.pingPong.application.usecase.PingPongUseCases
import com.turnin.domain.pingPong.domain.model.PingPongContentValidationException
import com.turnin.domain.pingPong.exception.PingPongException
import com.turnin.domain.pingPong.presentation.dto.CreatePingPongRequest
import com.turnin.util.testPlugin
import com.turnin.util.testPostEndpoint
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.junit.Test

class PingPongRouteTest {
    private val route = Api.V1.PingPong
    private val usecase: PingPongUseCases = mockk()

    @Test
    fun `핑퐁 작성 - 성공 시 201과 생성된 핑퐁을 반환한다`() = testApplication {
        coEvery {
            usecase.create(UserId(1L), any(), any())
        } returns PingPongDto(
            id = 10L,
            userKeywordId = 3L,
            questionerId = 1L,
            question = "질문 내용",
            createdAt = 1000L,
            updatedAt = 1000L,
        )

        testPostEndpoint(
            endpoint = route.byUserKeyword("3"),
            requestBody = CreatePingPongRequest(question = "질문 내용"),
            testPlugin = {
                testPlugin(
                    authRouting = { pingPongRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.Created,
            responseValidator = {
                containsAll("\"id\":10", "\"userKeywordId\":3", "\"questionerId\":1", "\"question\":\"질문 내용\"")
            },
        )
    }

    @Test
    fun `핑퐁 작성 - 토큰의 사용자 ID와 경로의 게시물 ID, 바디의 질문을 유스케이스에 전달한다`() = testApplication {
        coEvery {
            usecase.create(UserId(1L), any(), any())
        } returns TestPingPongDto

        testPostEndpoint(
            endpoint = route.byUserKeyword("3"),
            requestBody = CreatePingPongRequest(question = "질문 내용"),
            testPlugin = {
                testPlugin(
                    authRouting = { pingPongRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.Created,
            additionalAssertions = {
                coVerify(exactly = 1) { usecase.create(UserId(1L), 3L, "질문 내용") }
            },
        )
    }

    @Test
    fun `핑퐁 작성 - 토큰 없이 요청 시 401 에러를 반환한다`() = testApplication {
        testPostEndpoint(
            endpoint = route.byUserKeyword("3"),
            requestBody = TestCreatePingPongRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { pingPongRoutes(route, usecase) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    @Test
    fun `핑퐁 작성 - 게시물 ID가 숫자가 아니면 400 에러를 반환한다`() = testApplication {
        testPostEndpoint(
            endpoint = route.byUserKeyword("abc"),
            requestBody = TestCreatePingPongRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { pingPongRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.BadRequest,
        )
    }

    @Test
    fun `핑퐁 작성 - 요청 바디에 질문이 없으면 400 에러를 반환한다`() = testApplication {
        testPostEndpoint(
            endpoint = route.byUserKeyword("3"),
            requestBody = mapOf("content" to "질문 내용"),
            testPlugin = {
                testPlugin(
                    authRouting = { pingPongRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.BadRequest,
            responseValidator = {
                contains(CommonErrorCode.MalformedRequest.code)
            },
        )
    }

    @Test
    fun `핑퐁 작성 - 질문 내용 유효성 검사 실패 시 400 에러를 반환한다`() = testApplication {
        coEvery {
            usecase.create(UserId(1L), any(), any())
        } throws PingPongContentValidationException("핑퐁 내용이 비어있습니다.")

        testPostEndpoint(
            endpoint = route.byUserKeyword("3"),
            requestBody = CreatePingPongRequest(question = " "),
            testPlugin = {
                testPlugin(
                    authRouting = { pingPongRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.BadRequest,
            responseValidator = {
                containsAll(CommonErrorCode.ValidationDefault.code, "핑퐁 내용이 비어있습니다.")
            },
        )
    }

    @Test
    fun `핑퐁 작성 - 본인 게시물에 질문하면 400 에러를 반환한다`() = testApplication {
        val expectedException = PingPongException.CannotQuestionOwnUserKeyword()
        coEvery {
            usecase.create(UserId(1L), any(), any())
        } throws expectedException

        testPostEndpoint(
            endpoint = route.byUserKeyword("3"),
            requestBody = TestCreatePingPongRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { pingPongRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.BadRequest,
            responseValidator = {
                containsAll(
                    expectedException.errorCode.code,
                    expectedException.errorCode.description,
                )
            },
        )
    }

    @Test
    fun `핑퐁 작성 - 게시물을 조회할 수 없으면 404 에러를 반환한다`() = testApplication {
        val expectedException = PingPongException.UserKeywordNotFound()
        coEvery {
            usecase.create(UserId(1L), any(), any())
        } throws expectedException

        testPostEndpoint(
            endpoint = route.byUserKeyword("3"),
            requestBody = TestCreatePingPongRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { pingPongRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.NotFound,
            responseValidator = {
                containsAll(
                    expectedException.errorCode.code,
                    expectedException.errorCode.description,
                )
            },
        )
    }

    @Test
    fun `핑퐁 작성 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        val expectedException = ApiException(
            errorCode = CommonErrorCode.Unexpected,
            status = HttpStatusCode.InternalServerError,
            message = "unexpected error",
        )
        coEvery {
            usecase.create(UserId(1L), any(), any())
        } throws expectedException

        testPostEndpoint(
            endpoint = route.byUserKeyword("3"),
            requestBody = TestCreatePingPongRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { pingPongRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.InternalServerError,
            responseValidator = {
                containsAll(
                    expectedException.errorCode.code,
                    expectedException.errorCode.description,
                )
            },
        )
    }

    companion object {
        private val TestCreatePingPongRequest = CreatePingPongRequest(question = "question")
        private val TestPingPongDto = PingPongDto(
            id = 10L,
            userKeywordId = 3L,
            questionerId = 1L,
            question = "question",
            createdAt = 1000L,
            updatedAt = 1000L,
        )
    }
}
