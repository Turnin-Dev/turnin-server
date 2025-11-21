package com.peekr.domain.report.presentation.route

import com.peekr.common.exception.ApiException
import com.peekr.common.exception.common.CommonErrorCode
import com.peekr.common.exception.common.CommonException
import com.peekr.common.model.UserId
import com.peekr.common.route.Api
import com.peekr.domain.report.application.dto.ReportReasonDto
import com.peekr.domain.report.application.usecase.ReportUseCases
import com.peekr.domain.report.presentation.dto.ReportRequest
import com.peekr.util.testGetEndpoint
import com.peekr.util.testPlugin
import com.peekr.util.testPostEndpoint
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import org.junit.Test

class ReportRouteTest {
    private val route = Api.V1.Report
    private val usecase: ReportUseCases = mockk()

    @Test
    fun `신고 사유 조회 - 성공 테스트`() = testApplication {
        val expectedCount = 5
        coEvery {
            usecase.getReportReasons()
        } returns List(expectedCount) { TestReportReasonDto }

        testGetEndpoint(
            endpoint = "${route.ROUTE}${route.REASON}",
            queryParameters = null,
            testPlugin = {
                testPlugin(
                    authRouting = { reportRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
            responseValidator = {
                contains(TEST_REASON_DESC)
            },
        )
    }

    @Test
    fun `신고 사유 조회 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        val expectedApiException = ApiException(
            errorCode = CommonErrorCode.Unexpected,
            status = HttpStatusCode.InternalServerError,
            message = "unexpected error",
        )
        coEvery {
            usecase.getReportReasons()
        } throws expectedApiException

        testGetEndpoint(
            endpoint = "${route.ROUTE}${route.REASON}",
            queryParameters = null,
            testPlugin = {
                testPlugin(
                    authRouting = { reportRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = expectedApiException.status,
            responseValidator = {
                containsAll(
                    expectedApiException.errorCode.code,
                    expectedApiException.errorCode.description,
                )
            },
        )
    }

    @Test
    fun `신고 생성 - 성공 테스트`() = testApplication {
        coEvery {
            usecase.createReport(TestUserId, any())
        } just Runs

        testPostEndpoint(
            endpoint = route.ROUTE,
            queryParameters = null,
            requestBody = TestReportRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { reportRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.Created,
        )
    }

    @Test
    fun `신고 생성 - 요청자 ID와 신고자 ID가 다른 경우 요청에 실패하고 에러 바디로 응답한다`() = testApplication {
        val expectedException = CommonException.AccessDenied()
        coEvery {
            usecase.createReport(TestUserId, any())
        } throws expectedException

        testPostEndpoint(
            endpoint = route.ROUTE,
            queryParameters = null,
            requestBody = TestInvalidReportRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { reportRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = expectedException.status,
            responseValidator = {
                containsAll(
                    expectedException.errorCode.code,
                    expectedException.errorCode.description,
                )
            },
        )
    }

    @Test
    fun `신고 생성 - 토큰 없이 요청 시 401 에러를 반환한다`() = testApplication {
        coEvery {
            usecase.createReport(TestUserId, any())
        } just Runs

        testPostEndpoint(
            endpoint = route.ROUTE,
            queryParameters = null,
            requestBody = TestInvalidReportRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { reportRoutes(route, usecase) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    @Test
    fun `신고 생성 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        val expectedException = ApiException(
            errorCode = CommonErrorCode.Unexpected,
            status = HttpStatusCode.InternalServerError,
            message = "unexpected error",
        )
        coEvery {
            usecase.createReport(TestUserId, any())
        } throws expectedException

        testPostEndpoint(
            endpoint = route.ROUTE,
            queryParameters = null,
            requestBody = TestInvalidReportRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { reportRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = expectedException.status,
            responseValidator = {
                containsAll(
                    expectedException.errorCode.code,
                    expectedException.errorCode.description,
                )
            },
        )
    }

    @Test
    fun `신고 생성 - 잘못된 형식의 사용자 ID인 경우 BadRequest를 반환한다`() = testApplication {
        coEvery {
            usecase.createReport(TestUserId, any())
        } just Runs

        testPostEndpoint(
            endpoint = route.ROUTE,
            queryParameters = null,
            requestBody = TestReportRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { reportRoutes(route, usecase) },
                )
            },
            tokenSubject = INVALID_USER_ID,
            expectedStatus = HttpStatusCode.BadRequest,
        )
    }

    companion object {
        private val TestUserId = UserId(1L)
        private const val INVALID_USER_ID = "asd"
        private const val TEST_REASON_DESC = "test reason desc"
        private val TestReportReasonDto = ReportReasonDto(
            code = "code",
            description = TEST_REASON_DESC,
        )
        private val TestReportRequest = ReportRequest(
            reporterId = TestUserId.value,
            reportedId = 2L,
            reasonId = 5L,
            customReason = "custom reason",
        )
        private val TestInvalidReportRequest = ReportRequest(
            reporterId = 10L,
            reportedId = 2L,
            reasonId = 5L,
            customReason = "custom reason",
        )
    }
}
