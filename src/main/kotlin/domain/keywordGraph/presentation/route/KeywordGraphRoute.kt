package com.peekr.domain.keywordGraph.presentation.route

import com.peekr.common.plugin.AuthenticatedRoute
import com.peekr.common.route.Api
import com.peekr.common.util.pagination.cursor.CursorPage
import com.peekr.common.util.pagination.cursor.getCursorPaginationParams
import com.peekr.common.util.pagination.cursor.toResponse
import com.peekr.common.validator.inputValidationAndReturn
import com.peekr.domain.keywordGraph.application.usecase.KeywordGraphUseCases
import com.peekr.domain.keywordGraph.presentation.dto.NodeContextResponse
import com.peekr.domain.keywordGraph.presentation.dto.toResponse
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond

fun AuthenticatedRoute.keywordGraphRoutes(route: Api.V1.KeywordGraph, usecase: KeywordGraphUseCases) {
    route(route.ROUTE, {
        tags = setOf(route.TAG)
        description = "Keyword Graph API"
    }) {
        get({ getNodeContextDocs() }) {
            val userId = call.queryParameters["userId"]
                ?.toLongOrNull()
                .inputValidationAndReturn("사용자 ID")
            val cursorPaginationParams = getCursorPaginationParams()
            val cursorPage = usecase.getNodeContextUseCase(
                userId = userId,
                cursor = cursorPaginationParams.cursor,
                pageSize = cursorPaginationParams.size,
            )
            val response = cursorPage.toResponse { nodeContextDto ->
                nodeContextDto.toResponse()
            }
            call.respond(HttpStatusCode.OK, response)
        }
    }
}

private fun RouteConfig.getNodeContextDocs() {
    summary = "사용자 ID로 사용자 키워드 노드 목록 조회 (페이지네이션)"
    description = "사용자 ID로 사용자 키워드 노드 목록을 커서 기반 페이지네이션으로 조회한다."
    request {
        queryParameter<Long>("userId") {
            description = "사용자 ID"
        }
        queryParameter<Long?>("cursor") {
            description = "페이지네이션에 필요한 커서 값 (초기 호출 시 null 로 요청)"
        }
        queryParameter<Int>("size") {
            description = "페이지네이션에 필요한 페이지 크기"
        }
    }
    response {
        code(HttpStatusCode.OK) {
            description = "노드 컨텍스트 응답 바디"
            body<CursorPage<NodeContextResponse>> {
                example("CursorPage(NodeContextResponse)") {
                    value = NodeContextResponse.sample
                }
            }
        }
    }
}
