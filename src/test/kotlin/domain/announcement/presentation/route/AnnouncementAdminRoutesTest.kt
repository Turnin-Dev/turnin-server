package com.turnin.domain.announcement.presentation.route

import com.turnin.common.exception.ApiException
import com.turnin.common.exception.common.CommonErrorCode
import com.turnin.common.model.AnnouncementAudience
import com.turnin.common.model.AnnouncementStatus
import com.turnin.common.model.id.AnnouncementId
import com.turnin.common.model.id.UserId
import com.turnin.common.plugin.AuthRole
import com.turnin.common.route.Api
import com.turnin.domain.announcement.application.usecase.AnnouncementAdminUseCases
import com.turnin.domain.announcement.exception.AnnouncementException
import com.turnin.domain.announcement.presentation.dto.CreateAnnouncementRequest
import com.turnin.domain.announcement.presentation.dto.UpdateAnnouncementStatusRequest
import com.turnin.util.testDeleteEndpoint
import com.turnin.util.testPatchEndpoint
import com.turnin.util.testPlugin
import com.turnin.util.testPostEndpoint
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import org.junit.Test

class AnnouncementAdminRouteTest {
    private val route = Api.Admin.Announcement
    private val usecase: AnnouncementAdminUseCases = mockk()

    // =============== 공지 생성 ===============

    @Test
    fun `공지 생성 - 성공 테스트`() = testApplication {
        coEvery {
            usecase.create(any())
        } just Runs

        testPostEndpoint(
            role = AuthRole.ADMIN,
            endpoint = route.ROUTE,
            queryParameters = null,
            requestBody = TestCreateAnnouncementRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { announcementAdminRoutes(route, usecase) },
                )
            },
            tokenSubject = TestAdminId.value.toString(),
            expectedStatus = HttpStatusCode.Created,
        )
    }

    @Test
    fun `공지 생성 - 토큰 없이 요청 시 401을 반환한다`() = testApplication {
        testPostEndpoint(
            role = AuthRole.ADMIN,
            endpoint = route.ROUTE,
            queryParameters = null,
            requestBody = TestCreateAnnouncementRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { announcementAdminRoutes(route, usecase) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    @Test
    fun `공지 생성 - 일반 사용자 토큰으로 요청 시 401을 반환한다`() = testApplication {
        testPostEndpoint(
            role = AuthRole.USER,
            endpoint = route.ROUTE,
            queryParameters = null,
            requestBody = TestCreateAnnouncementRequest,
            testPlugin = {
                testPlugin(
                    role = AuthRole.ADMIN,
                    authRouting = { announcementAdminRoutes(route, usecase) },
                )
            },
            tokenSubject = TestAdminId.value.toString(),
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    @Test
    fun `공지 생성 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        val expectedException = ApiException(
            errorCode = CommonErrorCode.Unexpected,
            status = HttpStatusCode.InternalServerError,
            message = "unexpected error",
        )
        coEvery {
            usecase.create(any())
        } throws expectedException

        testPostEndpoint(
            role = AuthRole.ADMIN,
            endpoint = route.ROUTE,
            queryParameters = null,
            requestBody = TestCreateAnnouncementRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { announcementAdminRoutes(route, usecase) },
                )
            },
            tokenSubject = TestAdminId.value.toString(),
            expectedStatus = expectedException.status,
            responseValidator = {
                containsAll(
                    expectedException.errorCode.code,
                    expectedException.errorCode.description,
                )
            },
        )
    }

    // =============== 공지 상태 변경 ===============

    @Test
    fun `공지 상태 변경 - 성공 테스트`() = testApplication {
        coEvery {
            usecase.update(TestAnnouncementId, any())
        } just Runs

        testPatchEndpoint(
            role = AuthRole.ADMIN,
            endpoint = "${route.ROUTE}${route.STATUS}/${TestAnnouncementId.value}",
            queryParameters = null,
            requestBody = TestUpdateStatusRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { announcementAdminRoutes(route, usecase) },
                )
            },
            tokenSubject = TestAdminId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
        )
    }

    @Test
    fun `공지 상태 변경 - 토큰 없이 요청 시 401을 반환한다`() = testApplication {
        testPatchEndpoint(
            role = AuthRole.ADMIN,
            endpoint = "${route.ROUTE}${route.STATUS}/${TestAnnouncementId.value}",
            queryParameters = null,
            requestBody = TestUpdateStatusRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { announcementAdminRoutes(route, usecase) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    @Test
    fun `공지 상태 변경 - 잘못된 형식의 공지 ID인 경우 BadRequest를 반환한다`() = testApplication {
        testPatchEndpoint(
            role = AuthRole.ADMIN,
            endpoint = "${route.ROUTE}${route.STATUS}/invalid",
            queryParameters = null,
            requestBody = TestUpdateStatusRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { announcementAdminRoutes(route, usecase) },
                )
            },
            tokenSubject = TestAdminId.value.toString(),
            expectedStatus = HttpStatusCode.BadRequest,
        )
    }

    @Test
    fun `공지 상태 변경 - 존재하지 않는 공지인 경우 NotFound를 반환한다`() = testApplication {
        coEvery {
            usecase.update(TestAnnouncementId, any())
        } throws AnnouncementException.NotFound(TestAnnouncementId)

        testPatchEndpoint(
            role = AuthRole.ADMIN,
            endpoint = "${route.ROUTE}${route.STATUS}/${TestAnnouncementId.value}",
            queryParameters = null,
            requestBody = TestUpdateStatusRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { announcementAdminRoutes(route, usecase) },
                )
            },
            tokenSubject = TestAdminId.value.toString(),
            expectedStatus = HttpStatusCode.NotFound,
        )
    }

    // =============== 공지 삭제 ===============

    @Test
    fun `공지 삭제 - 성공 테스트`() = testApplication {
        coEvery {
            usecase.delete(TestAnnouncementId)
        } just Runs

        testDeleteEndpoint(
            role = AuthRole.ADMIN,
            endpoint = "${route.ROUTE}/${TestAnnouncementId.value}",
            queryParameters = null,
            testPlugin = {
                testPlugin(
                    authRouting = { announcementAdminRoutes(route, usecase) },
                )
            },
            tokenSubject = TestAdminId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
        )
    }

    @Test
    fun `공지 삭제 - 토큰 없이 요청 시 401을 반환한다`() = testApplication {
        testDeleteEndpoint(
            role = AuthRole.ADMIN,
            endpoint = "${route.ROUTE}/${TestAnnouncementId.value}",
            queryParameters = null,
            testPlugin = {
                testPlugin(
                    authRouting = { announcementAdminRoutes(route, usecase) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    @Test
    fun `공지 삭제 - 잘못된 형식의 공지 ID인 경우 BadRequest를 반환한다`() = testApplication {
        testDeleteEndpoint(
            role = AuthRole.ADMIN,
            endpoint = "${route.ROUTE}/invalid",
            queryParameters = null,
            testPlugin = {
                testPlugin(
                    authRouting = { announcementAdminRoutes(route, usecase) },
                )
            },
            tokenSubject = TestAdminId.value.toString(),
            expectedStatus = HttpStatusCode.BadRequest,
        )
    }

    @Test
    fun `공지 삭제 - 존재하지 않는 공지인 경우 NotFound를 반환한다`() = testApplication {
        coEvery {
            usecase.delete(TestAnnouncementId)
        } throws AnnouncementException.NotFound(TestAnnouncementId)

        testDeleteEndpoint(
            role = AuthRole.ADMIN,
            endpoint = "${route.ROUTE}/${TestAnnouncementId.value}",
            queryParameters = null,
            testPlugin = {
                testPlugin(
                    authRouting = { announcementAdminRoutes(route, usecase) },
                )
            },
            tokenSubject = TestAdminId.value.toString(),
            expectedStatus = HttpStatusCode.NotFound,
        )
    }

    companion object {
        private val TestAdminId = UserId(1L)
        private val TestAnnouncementId = AnnouncementId(1L)
        private val TestCreateAnnouncementRequest = CreateAnnouncementRequest(
            title = "테스트 공지",
            content = "테스트 내용",
            targetAudience = AnnouncementAudience.ALL,
            expiresAt = null,
        )
        private val TestUpdateStatusRequest = UpdateAnnouncementStatusRequest(
            status = AnnouncementStatus.ACTIVE,
        )
    }
}
