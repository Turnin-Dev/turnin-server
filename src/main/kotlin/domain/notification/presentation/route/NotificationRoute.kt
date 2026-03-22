package com.peekr.domain.notification.presentation.route

import com.peekr.common.exception.toErrorResponse
import com.peekr.common.plugin.AuthenticatedRoute
import com.peekr.common.route.Api
import com.peekr.common.util.pagination.cursor.CursorPage
import com.peekr.common.util.pagination.cursor.getCursorPaginationParams
import com.peekr.common.util.pagination.cursor.toResponse
import com.peekr.common.validator.inputValidationAndReturn
import com.peekr.domain.notification.application.usecase.NotificationUseCases
import com.peekr.domain.notification.exception.NotificationErrorCode
import com.peekr.domain.notification.presentation.dto.FcmTokenResponse
import com.peekr.domain.notification.presentation.dto.NotificationResponse
import com.peekr.domain.notification.presentation.dto.RegisterFcmTokenRequest
import com.peekr.domain.notification.presentation.dto.toResponse
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.patch
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond

fun AuthenticatedRoute.notificationRoutes(
    route: Api.V1.Notification,
    usecase: NotificationUseCases,
) {
    route(route.ROUTE, {
        tags = setOf(route.TAG)
        description = "Notification API"
    }) {
        // FCM 토큰 등록
        post(route.TOKEN, { registerFcmTokenDocs() }) {
            val userId = extractUserIdWithToken()
            val request = call.receive<RegisterFcmTokenRequest>()
            val result = usecase.registerToken(userId, request.token)
            call.respond(HttpStatusCode.OK, result.toResponse())
        }

        // FCM 토큰 비활성화 (로그아웃)
        delete(route.TOKEN, { deactivateFcmTokenDocs() }) {
            val userId = extractUserIdWithToken()
            val token = call.request.queryParameters["token"].inputValidationAndReturn("FCM 토큰")
            val success = usecase.deactivateToken(userId, token)
            if (success) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    NotificationErrorCode.FcmTokenNotFound.toErrorResponse(HttpStatusCode.NotFound),
                )
            }
        }

        // 알림 목록 조회
        get({ getNotificationsDocs() }) {
            val userId = extractUserIdWithToken()
            val params = getCursorPaginationParams()
            val cursorPage = usecase.getNotifications(
                userId = userId,
                cursor = params.cursor,
                pageSize = params.size,
            )
            val response = cursorPage.toResponse { notificationDto ->
                notificationDto.toResponse()
            }
            call.respond(HttpStatusCode.OK, response)
        }

        // 알림 읽음 처리
        patch(route.read("notificationId"), { markAsReadDocs() }) {
            val userId = extractUserIdWithToken()
            val notificationId = call.request.pathVariables["notificationId"]
                ?.toLongOrNull()
                .inputValidationAndReturn("알림 ID")

            val success = usecase.markAsRead(notificationId, userId)
            if (success) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    NotificationErrorCode.NotificationNotFound.toErrorResponse(HttpStatusCode.NotFound),
                )
            }
        }
    }
}

private fun RouteConfig.registerFcmTokenDocs() {
    summary = "FCM 토큰 등록"
    description = "로그인 및 회원가입 시 FCM 토큰을 등록한다."
    request {
        body<RegisterFcmTokenRequest> {
            description = "FCM 토큰 등록 요청 바디"
            example("RegisterFcmTokenRequest") {
                value = RegisterFcmTokenRequest.sample
            }
        }
    }
    response {
        code(HttpStatusCode.OK) {
            body<FcmTokenResponse> {
                description = "FCM 토큰 등록 성공 시"
                example("FcmTokenResponse") {
                    value = FcmTokenResponse.sample
                }
            }
        }
        code(HttpStatusCode.Unauthorized) {
            description = "인증 오류 시"
        }
    }
}

private fun RouteConfig.deactivateFcmTokenDocs() {
    summary = "FCM 토큰 비활성화"
    description = "로그아웃 시 FCM 토큰을 비활성화한다."
    request {
        queryParameter<String>("token") {
            description = "비활성화할 FCM 토큰"
            required = true
        }
    }
    response {
        code(HttpStatusCode.NoContent) {
            description = "FCM 토큰 비활성화 성공 시"
        }
        code(HttpStatusCode.NotFound) {
            description = "FCM 토큰을 찾을 수 없는 경우"
        }
        code(HttpStatusCode.BadRequest) {
            description = "token 파라미터가 누락된 경우"
        }
        code(HttpStatusCode.Unauthorized) {
            description = "인증 오류 시"
        }
    }
}

private fun RouteConfig.getNotificationsDocs() {
    summary = "알림 목록 조회"
    description = """
        개인 알림 및 브로드캐스트 알림을 최신순으로 조회한다. (커서 기반 페이지네이션)

        브로드캐스트 알림(isBroadcast = true)은 읽음 처리를 지원하지 않으므로
        클라이언트에서 isBroadcast 여부를 확인하여 읽음 처리 UI를 숨겨야 한다.
    """.trimIndent()
    request {
        queryParameter<Long?>("cursor") {
            description = "페이지네이션에 필요한 커서 값 (초기 호출 시 null 로 요청)"
        }
        queryParameter<Int>("size") {
            description = "페이지네이션에 필요한 페이지 크기"
        }
    }
    response {
        code(HttpStatusCode.OK) {
            body<CursorPage<NotificationResponse, Long>> {
                description = "알림 목록 조회 성공 시"
                example("CursorPage(NotificationResponse)") {
                    value = NotificationResponse.sample
                }
            }
        }
        code(HttpStatusCode.Unauthorized) {
            description = "인증 오류 시"
        }
    }
}

private fun RouteConfig.markAsReadDocs() {
    summary = "알림 읽음 처리"
    description = """
        특정 알림을 읽음 처리한다.

        브로드캐스트 알림(isBroadcast = true)은 userId가 null이므로 읽음 처리가 불가능하다.
        클라이언트에서 브로드캐스트 알림에 대한 읽음 처리 요청을 하지 않아야 한다.
    """.trimIndent()
    request {
        pathParameter<Long>("notificationId") {
            description = "읽음 처리할 알림 ID (브로드캐스트 알림 ID 전달 시 404 반환)"
        }
    }
    response {
        code(HttpStatusCode.NoContent) {
            description = "읽음 처리 성공 시"
        }
        code(HttpStatusCode.NotFound) {
            description = "알림을 찾을 수 없는 경우 (브로드캐스트 알림 포함)"
        }
        code(HttpStatusCode.Unauthorized) {
            description = "인증 오류 시"
        }
    }
}
