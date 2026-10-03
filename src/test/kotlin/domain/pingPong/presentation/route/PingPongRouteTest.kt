package com.turnin.domain.pingPong.presentation.route

import com.turnin.common.exception.ApiException
import com.turnin.common.exception.common.CommonErrorCode
import com.turnin.common.model.id.PingPongIdValidationException
import com.turnin.common.model.id.UserId
import com.turnin.common.route.Api
import com.turnin.common.util.pagination.cursor.CursorPage
import com.turnin.domain.pingPong.application.dto.PingPongAnswerDto
import com.turnin.domain.pingPong.application.dto.PingPongDetailDto
import com.turnin.domain.pingPong.application.dto.PingPongDto
import com.turnin.domain.pingPong.application.dto.PingPongQuestionerDto
import com.turnin.domain.pingPong.application.usecase.PingPongUseCases
import com.turnin.domain.pingPong.domain.model.PingPongContentValidationException
import com.turnin.domain.pingPong.exception.PingPongException
import com.turnin.domain.pingPong.presentation.dto.CreatePingPongAnswerRequest
import com.turnin.domain.pingPong.presentation.dto.CreatePingPongRequest
import com.turnin.util.testGetEndpoint
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

    @Test
    fun `핑퐁 답변 작성 - 성공 시 201과 생성된 답변을 반환한다`() = testApplication {
        coEvery {
            usecase.createAnswer(UserId(1L), any(), any())
        } returns PingPongAnswerDto(
            id = 20L,
            pingPongId = 10L,
            answer = "답변 내용",
            createdAt = 2000L,
            updatedAt = 2000L,
        )

        testPostEndpoint(
            endpoint = route.answer("10"),
            requestBody = CreatePingPongAnswerRequest(answer = "답변 내용"),
            testPlugin = {
                testPlugin(
                    authRouting = { pingPongRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.Created,
            responseValidator = {
                containsAll("\"id\":20", "\"pingPongId\":10", "\"answer\":\"답변 내용\"")
            },
        )
    }

    @Test
    fun `핑퐁 답변 작성 - 토큰의 사용자 ID와 경로의 핑퐁 ID, 바디의 답변을 유스케이스에 전달한다`() = testApplication {
        coEvery {
            usecase.createAnswer(UserId(1L), any(), any())
        } returns TestPingPongAnswerDto

        testPostEndpoint(
            endpoint = route.answer("10"),
            requestBody = CreatePingPongAnswerRequest(answer = "답변 내용"),
            testPlugin = {
                testPlugin(
                    authRouting = { pingPongRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.Created,
            additionalAssertions = {
                coVerify(exactly = 1) { usecase.createAnswer(UserId(1L), 10L, "답변 내용") }
            },
        )
    }

    @Test
    fun `핑퐁 답변 작성 - 토큰 없이 요청 시 401 에러를 반환한다`() = testApplication {
        testPostEndpoint(
            endpoint = route.answer("10"),
            requestBody = TestCreatePingPongAnswerRequest,
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
    fun `핑퐁 답변 작성 - 핑퐁 ID가 숫자가 아니면 400 에러를 반환한다`() = testApplication {
        testPostEndpoint(
            endpoint = route.answer("abc"),
            requestBody = TestCreatePingPongAnswerRequest,
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
    fun `핑퐁 답변 작성 - 요청 바디에 답변이 없으면 400 에러를 반환한다`() = testApplication {
        testPostEndpoint(
            endpoint = route.answer("10"),
            requestBody = mapOf("content" to "답변 내용"),
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
    fun `핑퐁 답변 작성 - 답변 내용 유효성 검사 실패 시 400 에러를 반환한다`() = testApplication {
        coEvery {
            usecase.createAnswer(UserId(1L), any(), any())
        } throws PingPongContentValidationException("핑퐁 내용이 비어있습니다.")

        testPostEndpoint(
            endpoint = route.answer("10"),
            requestBody = CreatePingPongAnswerRequest(answer = " "),
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
    fun `핑퐁 답변 작성 - 게시물 작성자가 아니면 403 에러를 반환한다`() = testApplication {
        val expectedException = PingPongException.NotUserKeywordOwner()
        coEvery {
            usecase.createAnswer(UserId(1L), any(), any())
        } throws expectedException

        testPostEndpoint(
            endpoint = route.answer("10"),
            requestBody = TestCreatePingPongAnswerRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { pingPongRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.Forbidden,
            responseValidator = {
                containsAll(
                    expectedException.errorCode.code,
                    expectedException.errorCode.description,
                )
            },
        )
    }

    @Test
    fun `핑퐁 답변 작성 - 질문자와 차단 관계이면 403 에러를 반환한다`() = testApplication {
        val expectedException = PingPongException.CannotAnswerBlockedQuestioner()
        coEvery {
            usecase.createAnswer(UserId(1L), any(), any())
        } throws expectedException

        testPostEndpoint(
            endpoint = route.answer("10"),
            requestBody = TestCreatePingPongAnswerRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { pingPongRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.Forbidden,
            responseValidator = {
                containsAll(
                    expectedException.errorCode.code,
                    expectedException.errorCode.description,
                )
            },
        )
    }

    @Test
    fun `핑퐁 답변 작성 - 핑퐁을 찾을 수 없으면 404 에러를 반환한다`() = testApplication {
        val expectedException = PingPongException.PingPongNotFound()
        coEvery {
            usecase.createAnswer(UserId(1L), any(), any())
        } throws expectedException

        testPostEndpoint(
            endpoint = route.answer("10"),
            requestBody = TestCreatePingPongAnswerRequest,
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
    fun `핑퐁 답변 작성 - 질문이 달린 게시물을 조회할 수 없으면 404 에러를 반환한다`() = testApplication {
        val expectedException = PingPongException.UserKeywordNotFound()
        coEvery {
            usecase.createAnswer(UserId(1L), any(), any())
        } throws expectedException

        testPostEndpoint(
            endpoint = route.answer("10"),
            requestBody = TestCreatePingPongAnswerRequest,
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
    fun `핑퐁 답변 작성 - 이미 답변이 등록된 질문이면 409 에러를 반환한다`() = testApplication {
        val expectedException = PingPongException.AlreadyAnswered()
        coEvery {
            usecase.createAnswer(UserId(1L), any(), any())
        } throws expectedException

        testPostEndpoint(
            endpoint = route.answer("10"),
            requestBody = TestCreatePingPongAnswerRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { pingPongRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.Conflict,
            responseValidator = {
                containsAll(
                    expectedException.errorCode.code,
                    expectedException.errorCode.description,
                )
            },
        )
    }

    @Test
    fun `핑퐁 답변 작성 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        val expectedException = ApiException(
            errorCode = CommonErrorCode.Unexpected,
            status = HttpStatusCode.InternalServerError,
            message = "unexpected error",
        )
        coEvery {
            usecase.createAnswer(UserId(1L), any(), any())
        } throws expectedException

        testPostEndpoint(
            endpoint = route.answer("10"),
            requestBody = TestCreatePingPongAnswerRequest,
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

    @Test
    fun `핑퐁 목록 조회 - 성공 시 200과 핑퐁 목록, 다음 커서를 반환한다`() = testApplication {
        coEvery {
            usecase.getPingPongs(UserId(1L), any(), any(), any())
        } returns CursorPage(
            items = listOf(
                PingPongDetailDto(
                    pingPong = PingPongDto(
                        id = 10L,
                        userKeywordId = 3L,
                        questionerId = 5L,
                        question = "질문 내용",
                        createdAt = 1000L,
                        updatedAt = 1000L,
                    ),
                    questioner = PingPongQuestionerDto(
                        userId = 5L,
                        userName = "질문자",
                        profileImageUrl = null,
                    ),
                    answer = PingPongAnswerDto(
                        id = 20L,
                        pingPongId = 10L,
                        answer = "답변 내용",
                        createdAt = 2000L,
                        updatedAt = 2000L,
                    ),
                ),
            ),
            nextCursor = 10L,
        )

        testGetEndpoint(
            endpoint = route.byUserKeyword("3"),
            queryParameters = mapOf("size" to "1"),
            testPlugin = {
                testPlugin(
                    authRouting = { pingPongRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.OK,
            responseValidator = {
                containsAll(
                    "\"question\":\"질문 내용\"",
                    "\"userName\":\"질문자\"",
                    "\"answer\":\"답변 내용\"",
                    "\"nextCursor\":10",
                )
            },
        )
    }

    @Test
    fun `핑퐁 목록 조회 - 토큰의 사용자 ID와 경로의 게시물 ID, 쿼리의 커서와 크기를 유스케이스에 전달한다`() = testApplication {
        coEvery {
            usecase.getPingPongs(UserId(1L), any(), any(), any())
        } returns CursorPage(emptyList(), null)

        testGetEndpoint(
            endpoint = route.byUserKeyword("3"),
            queryParameters = mapOf("cursor" to "50", "size" to "10"),
            testPlugin = {
                testPlugin(
                    authRouting = { pingPongRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.OK,
            additionalAssertions = {
                coVerify(exactly = 1) { usecase.getPingPongs(UserId(1L), 3L, 50L, 10) }
            },
        )
    }

    @Test
    fun `핑퐁 목록 조회 - 커서 없이 요청 시 커서를 null로 유스케이스에 전달한다`() = testApplication {
        coEvery {
            usecase.getPingPongs(UserId(1L), any(), any(), any())
        } returns CursorPage(emptyList(), null)

        testGetEndpoint(
            endpoint = route.byUserKeyword("3"),
            queryParameters = mapOf("size" to "10"),
            testPlugin = {
                testPlugin(
                    authRouting = { pingPongRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.OK,
            additionalAssertions = {
                coVerify(exactly = 1) { usecase.getPingPongs(UserId(1L), 3L, null, 10) }
            },
        )
    }

    @Test
    fun `핑퐁 목록 조회 - 토큰 없이 요청 시 401 에러를 반환한다`() = testApplication {
        testGetEndpoint(
            endpoint = route.byUserKeyword("3"),
            queryParameters = mapOf("size" to "10"),
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
    fun `핑퐁 목록 조회 - 게시물 ID가 숫자가 아니면 400 에러를 반환한다`() = testApplication {
        testGetEndpoint(
            endpoint = route.byUserKeyword("abc"),
            queryParameters = mapOf("size" to "10"),
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
    fun `핑퐁 목록 조회 - 페이지 크기 없이 요청 시 400 에러를 반환한다`() = testApplication {
        testGetEndpoint(
            endpoint = route.byUserKeyword("3"),
            queryParameters = null,
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
    fun `핑퐁 목록 조회 - 페이지 크기가 0이면 400 에러를 반환한다`() = testApplication {
        testGetEndpoint(
            endpoint = route.byUserKeyword("3"),
            queryParameters = mapOf("size" to "0"),
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
    fun `핑퐁 목록 조회 - 페이지 크기가 1이면 200을 반환한다`() = testApplication {
        coEvery {
            usecase.getPingPongs(UserId(1L), any(), any(), any())
        } returns CursorPage(emptyList(), null)

        testGetEndpoint(
            endpoint = route.byUserKeyword("3"),
            queryParameters = mapOf("size" to "1"),
            testPlugin = {
                testPlugin(
                    authRouting = { pingPongRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.OK,
        )
    }

    @Test
    fun `핑퐁 목록 조회 - 페이지 크기가 25이면 200을 반환한다`() = testApplication {
        coEvery {
            usecase.getPingPongs(UserId(1L), any(), any(), any())
        } returns CursorPage(emptyList(), null)

        testGetEndpoint(
            endpoint = route.byUserKeyword("3"),
            queryParameters = mapOf("size" to "25"),
            testPlugin = {
                testPlugin(
                    authRouting = { pingPongRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.OK,
        )
    }

    @Test
    fun `핑퐁 목록 조회 - 페이지 크기가 26이면 400 에러를 반환한다`() = testApplication {
        testGetEndpoint(
            endpoint = route.byUserKeyword("3"),
            queryParameters = mapOf("size" to "26"),
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
    fun `핑퐁 목록 조회 - 유효성 검사 실패 시 400 에러를 반환한다`() = testApplication {
        coEvery {
            usecase.getPingPongs(UserId(1L), any(), any(), any())
        } throws PingPongIdValidationException("핑퐁 ID는 0이나 음수가 될 수 없습니다.")

        testGetEndpoint(
            endpoint = route.byUserKeyword("3"),
            queryParameters = mapOf("cursor" to "0", "size" to "10"),
            testPlugin = {
                testPlugin(
                    authRouting = { pingPongRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.BadRequest,
            responseValidator = {
                containsAll(CommonErrorCode.ValidationDefault.code, "핑퐁 ID는 0이나 음수가 될 수 없습니다.")
            },
        )
    }

    @Test
    fun `핑퐁 목록 조회 - 게시물을 조회할 수 없으면 404 에러를 반환한다`() = testApplication {
        val expectedException = PingPongException.UserKeywordNotFound()
        coEvery {
            usecase.getPingPongs(UserId(1L), any(), any(), any())
        } throws expectedException

        testGetEndpoint(
            endpoint = route.byUserKeyword("3"),
            queryParameters = mapOf("size" to "10"),
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
    fun `핑퐁 목록 조회 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        val expectedException = ApiException(
            errorCode = CommonErrorCode.Unexpected,
            status = HttpStatusCode.InternalServerError,
            message = "unexpected error",
        )
        coEvery {
            usecase.getPingPongs(UserId(1L), any(), any(), any())
        } throws expectedException

        testGetEndpoint(
            endpoint = route.byUserKeyword("3"),
            queryParameters = mapOf("size" to "10"),
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
        private val TestCreatePingPongAnswerRequest = CreatePingPongAnswerRequest(answer = "answer")
        private val TestPingPongAnswerDto = PingPongAnswerDto(
            id = 20L,
            pingPongId = 10L,
            answer = "answer",
            createdAt = 2000L,
            updatedAt = 2000L,
        )
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
