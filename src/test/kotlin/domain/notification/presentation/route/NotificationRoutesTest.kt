package com.peekr.domain.notification.presentation.route

import com.peekr.common.exception.ApiException
import com.peekr.common.exception.common.CommonErrorCode
import com.peekr.common.model.NotificationType
import com.peekr.common.model.id.NotificationId
import com.peekr.common.model.id.UserId
import com.peekr.common.route.Api
import com.peekr.common.util.pagination.cursor.CursorPage
import com.peekr.domain.notification.application.dto.FcmTokenDto
import com.peekr.domain.notification.application.dto.NotificationDto
import com.peekr.domain.notification.application.usecase.NotificationUseCases
import com.peekr.domain.notification.exception.NotificationErrorCode
import com.peekr.domain.notification.presentation.dto.RegisterFcmTokenRequest
import com.peekr.util.testGetEndpoint
import com.peekr.util.testPatchEndpoint
import com.peekr.util.testPlugin
import com.peekr.util.testPostEndpoint
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.testApplication
import io.mockk.coEvery
import io.mockk.mockk
import org.junit.Test

class NotificationRoutesTest {
    private val route = Api.V1.Notification
    private val usecase: NotificationUseCases = mockk()

    // ======================== POST /token (FCM 토큰 등록) ========================

    @Test
    fun `FCM 토큰 등록 - 성공 테스트`() = testApplication {
        // given
        coEvery {
            usecase.registerToken(TestUserId, any())
        } returns TestFcmTokenDto

        // when, then
        testPostEndpoint(
            endpoint = "${route.ROUTE}/${route.TOKEN}",
            requestBody = TestRegisterFcmTokenRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { notificationRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
            responseValidator = {
                containsAll(
                    TestFcmTokenDto.token,
                )
            },
        )
    }

    @Test
    fun `FCM 토큰 등록 - 토큰 없이 요청 시 401 에러를 반환한다`() = testApplication {
        testPostEndpoint(
            endpoint = "${route.ROUTE}/${route.TOKEN}",
            requestBody = TestRegisterFcmTokenRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { notificationRoutes(route, usecase) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    @Test
    fun `FCM 토큰 등록 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        // given
        val expectedApiException = ApiException(
            errorCode = CommonErrorCode.Unexpected,
            status = HttpStatusCode.InternalServerError,
            message = "unexpected error",
        )
        coEvery {
            usecase.registerToken(TestUserId, any())
        } throws expectedApiException

        // when, then
        testPostEndpoint(
            endpoint = "${route.ROUTE}/${route.TOKEN}",
            requestBody = TestRegisterFcmTokenRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { notificationRoutes(route, usecase) },
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

    // ======================== PATCH /token (FCM 토큰 비활성화) ========================

    @Test
    fun `FCM 토큰 비활성화 - 성공 테스트`() = testApplication {
        // given
        coEvery {
            usecase.deactivateToken(TestUserId, any())
        } returns true

        // when, then
        testPatchEndpoint(
            endpoint = "${route.ROUTE}/${route.DEACTIVATE_TOKEN}",
            requestBody = TestRegisterFcmTokenRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { notificationRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.NoContent,
        )
    }

    @Test
    fun `FCM 토큰 비활성화 - 존재하지 않는 토큰 비활성화 시 404를 반환한다`() = testApplication {
        // given
        coEvery {
            usecase.deactivateToken(TestUserId, any())
        } returns false

        // when, then
        testPatchEndpoint(
            endpoint = "${route.ROUTE}/${route.DEACTIVATE_TOKEN}",
            requestBody = TestRegisterFcmTokenRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { notificationRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.NotFound,
            responseValidator = {
                containsAll(
                    NotificationErrorCode.FcmTokenNotFound.code,
                    NotificationErrorCode.FcmTokenNotFound.description,
                )
            },
        )
    }

    @Test
    fun `FCM 토큰 비활성화 - 토큰 없이 요청 시 401 에러를 반환한다`() = testApplication {
        testPatchEndpoint(
            endpoint = "${route.ROUTE}/${route.DEACTIVATE_TOKEN}",
            requestBody = TestRegisterFcmTokenRequest,
            testPlugin = {
                testPlugin(
                    authRouting = { notificationRoutes(route, usecase) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    // ======================== GET / (알림 목록 조회) ========================

    @Test
    fun `알림 목록 조회 - 성공 테스트`() = testApplication {
        // given
        val cursorPage = CursorPage(
            items = List(3) {
                NotificationDto(
                    id = it + 1L,
                    userId = TestUserId.value,
                    notiType = NotificationType.FRIEND_REQUEST,
                    title = "친구 요청 ${it + 1}",
                    message = "message ${it + 1}",
                    imageUrl = null,
                    isRead = false,
                    isBroadcast = false,
                    refId = it + 1L,
                    refType = "USER",
                    createdAt = 1716000000L,
                    updatedAt = 1716000000L,
                )
            },
            nextCursor = 2L,
        )
        coEvery {
            usecase.getNotifications(TestUserId, any(), any())
        } returns cursorPage

        // when, then
        testGetEndpoint(
            endpoint = route.ROUTE,
            queryParameters = mapOf(
                "cursor" to "1",
                "size" to "10",
            ),
            testPlugin = {
                testPlugin(
                    authRouting = { notificationRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.OK,
            responseValidator = {
                containsAll(
                    cursorPage.nextCursor.toString(),
                    cursorPage.items.first().title!!,
                    cursorPage.items.last().title!!,
                )
            },
        )
    }

    @Test
    fun `알림 목록 조회 - 토큰 없이 요청 시 401 에러를 반환한다`() = testApplication {
        testGetEndpoint(
            endpoint = route.ROUTE,
            queryParameters = mapOf(
                "cursor" to "1",
                "size" to "10",
            ),
            testPlugin = {
                testPlugin(
                    authRouting = { notificationRoutes(route, usecase) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    @Test
    fun `알림 목록 조회 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        // given
        val expectedApiException = ApiException(
            errorCode = CommonErrorCode.Unexpected,
            status = HttpStatusCode.InternalServerError,
            message = "unexpected error",
        )
        coEvery {
            usecase.getNotifications(TestUserId, any(), any())
        } throws expectedApiException

        // when, then
        testGetEndpoint(
            endpoint = route.ROUTE,
            queryParameters = mapOf(
                "cursor" to "1",
                "size" to "10",
            ),
            testPlugin = {
                testPlugin(
                    authRouting = { notificationRoutes(route, usecase) },
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

    // ======================== PATCH /{notificationId}/read (읽음 처리) ========================

    @Test
    fun `알림 읽음 처리 - 성공 테스트`() = testApplication {
        // given
        coEvery {
            usecase.markAsRead(TestNotificationId.value, TestUserId)
        } returns true

        // when, then
        testPatchEndpoint(
            endpoint = "${route.ROUTE}/1/${route.READ}",
            testPlugin = {
                testPlugin(
                    authRouting = { notificationRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.NoContent,
        )
    }

    @Test
    fun `알림 읽음 처리 - 존재하지 않는 알림 읽음 처리 시 404를 반환한다`() = testApplication {
        // given
        coEvery {
            usecase.markAsRead(TestNotificationId.value, TestUserId)
        } returns false

        // when, then
        testPatchEndpoint(
            endpoint = "${route.ROUTE}/1/${route.READ}",
            testPlugin = {
                testPlugin(
                    authRouting = { notificationRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.NotFound,
            responseValidator = {
                containsAll(
                    NotificationErrorCode.NotificationNotFound.code,
                    NotificationErrorCode.NotificationNotFound.description,
                )
            },
        )
    }

    @Test
    fun `알림 읽음 처리 - 잘못된 notificationId 형식으로 요청 시 400을 반환한다`() = testApplication {
        // when, then
        testPatchEndpoint(
            endpoint = "${route.ROUTE}/invalid/${route.READ}",
            testPlugin = {
                testPlugin(
                    authRouting = { notificationRoutes(route, usecase) },
                )
            },
            tokenSubject = TestUserId.value.toString(),
            expectedStatus = HttpStatusCode.BadRequest,
        )
    }

    @Test
    fun `알림 읽음 처리 - 토큰 없이 요청 시 401 에러를 반환한다`() = testApplication {
        testPatchEndpoint(
            endpoint = "${route.ROUTE}/1/${route.READ}",
            testPlugin = {
                testPlugin(
                    authRouting = { notificationRoutes(route, usecase) },
                )
            },
            tokenSubject = null,
            expectedStatus = HttpStatusCode.Unauthorized,
        )
    }

    @Test
    fun `알림 읽음 처리 - 예외 발생 시 정상적으로 에러 바디를 반환한다`() = testApplication {
        // given
        val expectedApiException = ApiException(
            errorCode = CommonErrorCode.Unexpected,
            status = HttpStatusCode.InternalServerError,
            message = "unexpected error",
        )
        coEvery {
            usecase.markAsRead(TestNotificationId.value, TestUserId)
        } throws expectedApiException

        // when, then
        testPatchEndpoint(
            endpoint = "${route.ROUTE}/1/${route.READ}",
            testPlugin = {
                testPlugin(
                    authRouting = { notificationRoutes(route, usecase) },
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

    companion object {
        private val TestUserId = UserId(1L)
        private val TestNotificationId = NotificationId(1L)
        private val TestRegisterFcmTokenRequest = RegisterFcmTokenRequest(
            token = "test_fcm_token",
        )
        private val TestFcmTokenDto = FcmTokenDto(
            id = 1L,
            userId = TestUserId.value,
            token = "test_fcm_token",
            isActive = true,
            createdAt = 1716000000L,
            updatedAt = 1716000000L,
        )
    }
}
