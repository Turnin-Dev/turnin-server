package com.turnin.domain.contentReport.presentation.route

import com.turnin.common.exception.common.CommonErrorCode
import com.turnin.common.plugin.AuthenticatedRoute
import com.turnin.common.route.Api
import com.turnin.domain.contentReport.application.usecase.ContentReportUseCases
import com.turnin.domain.contentReport.domain.model.ContentReportPolicy
import com.turnin.domain.contentReport.exception.ContentReportErrorCode
import com.turnin.domain.contentReport.presentation.dto.CreateContentReportRequest
import com.turnin.domain.contentReport.presentation.dto.toDto
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond

fun AuthenticatedRoute.contentReportRoutes(route: Api.V1.ContentReport, usecase: ContentReportUseCases) {
    route(route.ROUTE, {
        tags = setOf(route.TAG)
        description = "ContentReport API"
    }) {
        post({ createContentReportDocs() }) {
            val reporterId = extractUserIdWithToken()
            val createContentReportRequest = call.receive<CreateContentReportRequest>()
            usecase.create(reporterId, createContentReportRequest.toDto())
            call.respond(HttpStatusCode.Created)
        }
    }
}

private fun RouteConfig.createContentReportDocs() {
    summary = "콘텐츠 신고"
    description = """
        콘텐츠(핑퐁 질문/답변)를 신고한다. 신고 시점의 콘텐츠 내용은 스냅샷으로 함께 보관된다.

        - `contentType`에 따라 `contentId`가 가리키는 대상이 다르다. (Ex. `PING_PONG_QUESTION`: 핑퐁 ID, `PING_PONG_ANSWER`: 핑퐁 답변 ID)
        - 신고 사유는 `GET /report/reason`으로 조회한 목록을 사용한다.
        - 동일 콘텐츠는 한 번만 신고할 수 있으며, 차단 관계여도 신고할 수 있다.
        - 신고가 ${ContentReportPolicy.HIDE_THRESHOLD}회 누적되면 콘텐츠가 숨김 처리된다. (질문이 숨김 처리되면 핑퐁 전체가, 답변이 숨김 처리되면 답변만 목록에서 제외)
    """.trimIndent()
    request {
        body<CreateContentReportRequest> {
            description = "콘텐츠 신고 요청 바디"
            example("CreateContentReportRequest") {
                value = CreateContentReportRequest.sample
            }
        }
    }
    response {
        code(HttpStatusCode.Created) {
            description = "신고 성공"
        }
        code(HttpStatusCode.BadRequest) {
            description = """
                콘텐츠 ID 또는 신고 사유 ID가 0 이하인 경우 (`${CommonErrorCode.ValidationDefault.code}`),
                요청 바디 형식이 잘못되었거나 지원하지 않는 콘텐츠 유형인 경우 (`${CommonErrorCode.MalformedRequest.code}`)

                - UI 메시지: "요청을 처리할 수 없어요. 잠시 후 다시 시도해 주세요."

                본인이 작성한 콘텐츠를 신고하려는 경우 (`${ContentReportErrorCode.CannotReportOwnContent.code}`)

                - UI 메시지: "내가 작성한 글은 신고할 수 없어요."

                존재하지 않는 신고 사유인 경우 (`${ContentReportErrorCode.InvalidReportReason.code}`)

                - UI 메시지: "신고 사유를 다시 선택해 주세요."
            """.trimIndent()
        }
        code(HttpStatusCode.NotFound) {
            description = """
                신고할 콘텐츠가 없거나 이미 숨김 처리된 경우 (`${ContentReportErrorCode.ContentNotFound.code}`)

                - UI 메시지: "삭제되었거나 볼 수 없는 글이에요."
            """.trimIndent()
        }
        code(HttpStatusCode.Conflict) {
            description = """
                이미 신고한 콘텐츠인 경우 (`${ContentReportErrorCode.AlreadyReported.code}`)

                - UI 메시지: "이미 신고한 글이에요."
            """.trimIndent()
        }
    }
}
