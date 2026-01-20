package com.peekr.domain.report.presentation.route

import com.peekr.common.db.DatabaseException
import com.peekr.common.plugin.AuthenticatedRoute
import com.peekr.common.route.Api
import com.peekr.domain.report.application.usecase.ReportUseCases
import com.peekr.domain.report.presentation.dto.ReportReasonsResponse
import com.peekr.domain.report.presentation.dto.ReportRequest
import com.peekr.domain.report.presentation.dto.toDto
import com.peekr.domain.report.presentation.dto.toResponse
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond

fun AuthenticatedRoute.reportRoutes(route: Api.V1.Report, usecase: ReportUseCases) {
    route(route.ROUTE, {
        tags = setOf(route.TAG)
        description = "Report API"
    }) {
        get(route.REASON, { getReportReasonsDocs() }) {
            val reasons = usecase.getReportReasons()
            call.respond(reasons.toResponse())
        }

        post({ createReportDocs() }) {
            val ownerId = extractUserIdWithToken()
            val reportRequest = call.receive<ReportRequest>()
            try {
                usecase.createReport(ownerId, reportRequest.toDto())
                call.respond(HttpStatusCode.Created)
            } catch (_: DatabaseException.DuplicatedDataException) {
                call.respond(HttpStatusCode.Conflict)
            }
        }
    }
}

private fun RouteConfig.getReportReasonsDocs() {
    summary = "신고 사유 목록 조회"
    description = "신고 사유 목록을 조회한다."
    response {
        code(HttpStatusCode.OK) {
            body<ReportReasonsResponse> {
                description = "신고 사유 목록"
                example("ReportReasonsResponse") {
                    value = ReportReasonsResponse.sample
                }
            }
        }
    }
}

private fun RouteConfig.createReportDocs() {
    summary = "신고 생성"
    description = "신고 요청을 하면 신고를 생성한다.\n" +
        "신고 대상(사용자, 키워드)중 하나를 반드시 신고해야 한다."
    request {
        body<ReportRequest> {
            description = "신고 요청 바디"
            example("ReportRequest") {
                value = ReportRequest.sample
            }
        }
    }
    response {
        code(HttpStatusCode.Created) {
            description = "신고 요청 성공 시"
        }
        code(HttpStatusCode.Conflict) {
            description = "중복 신고 요청 시 (클라이언트에서 이를 별도로 처리해줘야 한다.)"
        }
        code(HttpStatusCode.Forbidden) {
            description = "요청자 ID와 신고자 ID가 일치하지 않는 경우 혹은 인증 오류 시"
        }
    }
}
