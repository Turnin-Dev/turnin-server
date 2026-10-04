package com.turnin.domain.contentReport.presentation.route

import com.turnin.common.exception.ApiException
import com.turnin.common.exception.common.CommonErrorCode
import com.turnin.common.model.ContentReportType
import com.turnin.common.model.id.ReportReasonIdValidationException
import com.turnin.common.model.id.UserId
import com.turnin.common.route.Api
import com.turnin.domain.contentReport.application.dto.ContentReportDto
import com.turnin.domain.contentReport.application.usecase.ContentReportUseCases
import com.turnin.domain.contentReport.exception.ContentReportException
import com.turnin.domain.contentReport.presentation.dto.CreateContentReportRequest
import com.turnin.util.testPlugin
import com.turnin.util.testPostEndpoint
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import kotlinx.serialization.Serializable
import org.junit.Test

class ContentReportRouteTest {
    private val route = Api.V1.ContentReport
    private val usecase: ContentReportUseCases = mockk()

    @Test
    fun `콘텐츠 신고 - 성공 시 201을 반환한다`() = testApplication {
        coEvery { usecase.create(UserId(1L), any()) } just Runs

        testPostEndpoint(
            endpoint = route.ROUTE,
            requestBody = TestCreateContentReportRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { contentReportRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.Created,
        )
    }

    @Test
    fun `콘텐츠 신고 - 토큰의 사용자 ID와 바디의 신고 내용을 유스케이스에 전달한다`() = testApplication {
        coEvery { usecase.create(UserId(1L), any()) } just Runs

        testPostEndpoint(
            endpoint = route.ROUTE,
            requestBody = CreateContentReportRequest(
                contentType = ContentReportType.PING_PONG_ANSWER,
                contentId = 20L,
                reasonId = 3L,
                customReason = "기타 사유",
            ),
            testPlugin = {
                testPlugin(
                    authRouting = { contentReportRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.Created,
            additionalAssertions = {
                coVerify(exactly = 1) {
                    usecase.create(
                        UserId(1L),
                        ContentReportDto(
                            contentType = ContentReportType.PING_PONG_ANSWER,
                            contentId = 20L,
                            reasonId = 3L,
                            customReason = "기타 사유",
                        ),
                    )
                }
            },
        )
    }

    @Test
    fun `콘텐츠 신고 - 토큰 없이 요청 시 401 에러를 반환한다`() = testApplication {
        testPostEndpoint(
            endpoint = route.ROUTE,
            requestBody = TestCreateContentReportRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { contentReportRoutes(route, usecase) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    @Test
    fun `콘텐츠 신고 - 요청 바디에 콘텐츠 유형이 없으면 400 에러를 반환한다`() = testApplication {
        testPostEndpoint(
            endpoint = route.ROUTE,
            requestBody = MissingContentTypeRequest(contentId = 10L, reasonId = 1L),
            testPlugin = {
                testPlugin(
                    authRouting = { contentReportRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.BadRequest,
        )
    }

    @Test
    fun `콘텐츠 신고 - 지원하지 않는 콘텐츠 유형이면 400 에러를 반환한다`() = testApplication {
        testPostEndpoint(
            endpoint = route.ROUTE,
            requestBody = RawContentTypeRequest(contentType = "USER_KEYWORD", contentId = 10L, reasonId = 1L),
            testPlugin = {
                testPlugin(
                    authRouting = { contentReportRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.BadRequest,
        )
    }

    @Test
    fun `콘텐츠 신고 - 유효성 검사 실패 시 400 에러를 반환한다`() = testApplication {
        coEvery {
            usecase.create(UserId(1L), any())
        } throws ReportReasonIdValidationException("신고 사유 ID는 0이나 음수가 될 수 없습니다.")

        testPostEndpoint(
            endpoint = route.ROUTE,
            requestBody = TestCreateContentReportRequest.copy(reasonId = 0L),
            testPlugin = {
                testPlugin(
                    authRouting = { contentReportRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.BadRequest,
            responseValidator = {
                containsAll(CommonErrorCode.ValidationDefault.code, "신고 사유 ID는 0이나 음수가 될 수 없습니다.")
            },
        )
    }

    @Test
    fun `콘텐츠 신고 - 본인이 작성한 콘텐츠를 신고하면 400 에러를 반환한다`() = testApplication {
        val expectedException = ContentReportException.CannotReportOwnContent()
        coEvery { usecase.create(UserId(1L), any()) } throws expectedException

        testPostEndpoint(
            endpoint = route.ROUTE,
            requestBody = TestCreateContentReportRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { contentReportRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.BadRequest,
            responseValidator = {
                containsAll(expectedException.errorCode.code, expectedException.errorCode.description)
            },
        )
    }

    @Test
    fun `콘텐츠 신고 - 존재하지 않는 신고 사유이면 400 에러를 반환한다`() = testApplication {
        val expectedException = ContentReportException.InvalidReportReason()
        coEvery { usecase.create(UserId(1L), any()) } throws expectedException

        testPostEndpoint(
            endpoint = route.ROUTE,
            requestBody = TestCreateContentReportRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { contentReportRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.BadRequest,
            responseValidator = {
                containsAll(expectedException.errorCode.code, expectedException.errorCode.description)
            },
        )
    }

    @Test
    fun `콘텐츠 신고 - 신고할 콘텐츠를 찾을 수 없으면 404 에러를 반환한다`() = testApplication {
        val expectedException = ContentReportException.ContentNotFound()
        coEvery { usecase.create(UserId(1L), any()) } throws expectedException

        testPostEndpoint(
            endpoint = route.ROUTE,
            requestBody = TestCreateContentReportRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { contentReportRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.NotFound,
            responseValidator = {
                containsAll(expectedException.errorCode.code, expectedException.errorCode.description)
            },
        )
    }

    @Test
    fun `콘텐츠 신고 - 이미 신고한 콘텐츠이면 409 에러를 반환한다`() = testApplication {
        val expectedException = ContentReportException.AlreadyReported()
        coEvery { usecase.create(UserId(1L), any()) } throws expectedException

        testPostEndpoint(
            endpoint = route.ROUTE,
            requestBody = TestCreateContentReportRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { contentReportRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.Conflict,
            responseValidator = {
                containsAll(expectedException.errorCode.code, expectedException.errorCode.description)
            },
        )
    }

    @Test
    fun `콘텐츠 신고 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        val expectedException = ApiException(
            errorCode = CommonErrorCode.Unexpected,
            status = HttpStatusCode.InternalServerError,
            message = "unexpected error",
        )
        coEvery { usecase.create(UserId(1L), any()) } throws expectedException

        testPostEndpoint(
            endpoint = route.ROUTE,
            requestBody = TestCreateContentReportRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { contentReportRoutes(route, usecase) },
                )
            },
            tokenSubject = "1",
            expectedStatus = HttpStatusCode.InternalServerError,
            responseValidator = {
                containsAll(expectedException.errorCode.code, expectedException.errorCode.description)
            },
        )
    }

    companion object {
        private val TestCreateContentReportRequest = CreateContentReportRequest(
            contentType = ContentReportType.PING_PONG_QUESTION,
            contentId = 10L,
            reasonId = 1L,
        )
    }
}

/** 콘텐츠 유형이 빠진 요청 바디 */
@Serializable
private data class MissingContentTypeRequest(
    val contentId: Long,
    val reasonId: Long,
)

/** 콘텐츠 유형을 문자열로 지정하는 요청 바디 */
@Serializable
private data class RawContentTypeRequest(
    val contentType: String,
    val contentId: Long,
    val reasonId: Long,
)
