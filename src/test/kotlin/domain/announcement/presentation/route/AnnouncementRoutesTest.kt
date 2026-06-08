package com.turnin.domain.announcement.presentation.route

import com.turnin.common.exception.ApiException
import com.turnin.common.exception.common.CommonErrorCode
import com.turnin.common.model.AnnouncementAudience
import com.turnin.common.model.id.AnnouncementId
import com.turnin.common.model.id.UserId
import com.turnin.common.route.Api
import com.turnin.domain.announcement.application.dto.AnnouncementDto
import com.turnin.domain.announcement.application.usecase.AnnouncementUseCases
import com.turnin.util.testGetEndpoint
import com.turnin.util.testPlugin
import com.turnin.util.testPostEndpoint
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.just
import io.mockk.mockk
import org.junit.Test

class AnnouncementRouteTest {
    private val route = Api.V1.Announcement
    private val usecase: AnnouncementUseCases = mockk()

    @Test
    fun `공지 목록 조회 - 성공 테스트`() = testApplication {
        coEvery {
            usecase.getAnnouncements(TestUserId, any())
        } returns listOf(TestAnnouncementDto)

        testGetEndpoint(
            endpoint = route.ROUTE,
            queryParameters = null,
            testPlugin = {
                testPlugin(
                    authRouting = { announcementRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
            responseValidator = {
                contains(TestAnnouncementDto.title)
            },
        )
    }

    @Test
    fun `공지 목록 조회 - 토큰 없이 요청 시 401을 반환한다`() = testApplication {
        testGetEndpoint(
            endpoint = route.ROUTE,
            queryParameters = null,
            testPlugin = {
                testPlugin(
                    authRouting = { announcementRoutes(route, usecase) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    @Test
    fun `공지 목록 조회 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        val expectedException = ApiException(
            errorCode = CommonErrorCode.Unexpected,
            status = HttpStatusCode.InternalServerError,
            message = "unexpected error",
        )
        coEvery {
            usecase.getAnnouncements(TestUserId, any())
        } throws expectedException

        testGetEndpoint(
            endpoint = route.ROUTE,
            queryParameters = null,
            testPlugin = {
                testPlugin(
                    authRouting = { announcementRoutes(route, usecase) },
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
    fun `공지 읽음 처리 - 성공 테스트`() = testApplication {
        coEvery {
            usecase.markAsRead(TestAnnouncementId, TestUserId)
        } just Runs

        testPostEndpoint(
            endpoint = "${route.ROUTE}${route.READ}/${TestAnnouncementId.value}",
            queryParameters = null,
            requestBody = null,
            testPlugin = {
                testPlugin(
                    authRouting = { announcementRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
        )
    }

    @Test
    fun `공지 읽음 처리 - 토큰 없이 요청 시 401을 반환한다`() = testApplication {
        testPostEndpoint(
            endpoint = "${route.ROUTE}${route.READ}/${TestAnnouncementId.value}",
            queryParameters = null,
            requestBody = null,
            testPlugin = {
                testPlugin(
                    authRouting = { announcementRoutes(route, usecase) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    @Test
    fun `공지 읽음 처리 - 잘못된 형식의 공지 ID인 경우 BadRequest를 반환한다`() = testApplication {
        testPostEndpoint(
            endpoint = "${route.ROUTE}${route.READ}/invalid",
            queryParameters = null,
            requestBody = null,
            testPlugin = {
                testPlugin(
                    authRouting = { announcementRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.BadRequest,
        )
    }

    @Test
    fun `공지 읽음 처리 - 잘못된 형식의 사용자 ID인 경우 BadRequest를 반환한다`() = testApplication {
        testPostEndpoint(
            endpoint = "${route.ROUTE}${route.READ}/${TestAnnouncementId.value}",
            queryParameters = null,
            requestBody = null,
            testPlugin = {
                testPlugin(
                    authRouting = { announcementRoutes(route, usecase) },
                )
            },
            tokenSubject = INVALID_USER_ID,
            expectedStatus = HttpStatusCode.BadRequest,
        )
    }

    @Test
    fun `공지 읽음 처리 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        val expectedException = ApiException(
            errorCode = CommonErrorCode.Unexpected,
            status = HttpStatusCode.InternalServerError,
            message = "unexpected error",
        )
        coEvery {
            usecase.markAsRead(TestAnnouncementId, TestUserId)
        } throws expectedException

        testPostEndpoint(
            endpoint = "${route.ROUTE}${route.READ}/${TestAnnouncementId.value}",
            queryParameters = null,
            requestBody = null,
            testPlugin = {
                testPlugin(
                    authRouting = { announcementRoutes(route, usecase) },
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

    companion object {
        private val TestUserId = UserId(1L)
        private val TestAnnouncementId = AnnouncementId(1L)
        private const val INVALID_USER_ID = "asd"
        private val TestAnnouncementDto = AnnouncementDto(
            id = 1L,
            title = "테스트 공지",
            content = "테스트 내용",
            targetAudience = AnnouncementAudience.ALL,
            expiresAt = null,
            createdAt = 1000L,
            isRead = false,
        )
    }
}
