package com.turnin.domain.notification.presentation.route

import com.turnin.common.exception.toErrorResponse
import com.turnin.common.firebase.FcmDataKey
import com.turnin.common.firebase.RefType
import com.turnin.common.plugin.AuthenticatedRoute
import com.turnin.common.route.Api
import com.turnin.common.util.pagination.cursor.CursorPage
import com.turnin.common.util.pagination.cursor.getCursorPaginationParams
import com.turnin.common.util.pagination.cursor.toResponse
import com.turnin.common.validator.inputValidationAndReturn
import com.turnin.domain.notification.application.usecase.NotificationUseCases
import com.turnin.domain.notification.exception.NotificationErrorCode
import com.turnin.domain.notification.presentation.dto.FcmTokenResponse
import com.turnin.domain.notification.presentation.dto.NotificationResponse
import com.turnin.domain.notification.presentation.dto.RegisterFcmTokenRequest
import com.turnin.domain.notification.presentation.dto.toResponse
import io.github.smiley4.ktoropenapi.config.RouteConfig
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
        patch(route.DEACTIVATE_TOKEN, { deactivateFcmTokenDocs() }) {
            val userId = extractUserIdWithToken()
            val request = call.receive<RegisterFcmTokenRequest>()
            usecase.deactivateToken(userId, request.token)
            call.respond(HttpStatusCode.NoContent)
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
        body<RegisterFcmTokenRequest> {
            description = "FCM 토큰 비활성화 요청 바디"
            example("RegisterFcmTokenRequest") {
                value = RegisterFcmTokenRequest.sample
            }
        }
    }
    response {
        code(HttpStatusCode.NoContent) {
            description = "FCM 토큰 비활성화 성공 시"
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

        ### 딥링크 데이터

        알림 클릭 시 이동할 화면의 핵심 ID는 `refType` + `refId`에, 그 외 화면 이동에 필요한 값은 `refData`(key-value)에 담는다.
        `userId`는 알림 **수신자**(본인) ID이므로 딥링크 파라미터로 사용하지 않는다.

        | notiType | 이동 화면 | refType | refId | refData |
        |---|---|---|---|---|
        | `FRIEND_REQUEST` | 요청한 사용자 프로필 | `${RefType.USER}` | 요청한 사용자 ID | - |
        | `FRIEND_ACCEPT` | 수락한 사용자 프로필 | `${RefType.USER}` | 수락한 사용자 ID | - |
        | `PING_PONG_QUESTION` | 키워드 게시물 상세 | `${RefType.KEYWORD}` | 사용자 키워드 ID | `${FcmDataKey.REF_OWNER_ID}`: 게시물 작성자 ID |
        | `PING_PONG_ANSWER` | 키워드 게시물 상세 | `${RefType.KEYWORD}` | 사용자 키워드 ID | `${FcmDataKey.REF_OWNER_ID}`: 게시물 작성자 ID |
        | `NOTICE`, `EVENT` | 알림 목록 | - | - | - |

        - 처음 보는 `notiType`/`refType`은 무시하고 알림 목록 화면으로 이동해야 한다. (신규 유형 추가 시 구버전 앱 호환)

        ### 푸시(FCM data) 키

        푸시는 data-only 메시지로 전송되며, 모든 값은 문자열이다. 알림 목록 응답과 같은 값을 아래 키로 담는다.

        | 키 | 값 |
        |---|---|
        | `${FcmDataKey.NOTI_TYPE}` | notiType |
        | `${FcmDataKey.TITLE}` | title |
        | `${FcmDataKey.BODY}` | message |
        | `${FcmDataKey.REF_TYPE}` | refType (없으면 키 생략) |
        | `${FcmDataKey.REF_ID}` | refId (없으면 키 생략) |
        | refData의 각 키 | refData의 각 값 (예: `${FcmDataKey.REF_OWNER_ID}`) |

        `NEW_KEYWORD`는 푸시로만 전송되며 알림 목록에 저장되지 않는다.
        (`${FcmDataKey.REF_TYPE}`: `${RefType.KEYWORD}`, `${FcmDataKey.REF_ID}`: 사용자 키워드 ID, `${FcmDataKey.REF_OWNER_ID}`: 게시물 작성자 ID,
        `user_id`: `${FcmDataKey.REF_OWNER_ID}`와 같은 값 - 구버전 앱 호환용, 제거 예정이므로 신규 구현은 `${FcmDataKey.REF_OWNER_ID}` 사용)
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
