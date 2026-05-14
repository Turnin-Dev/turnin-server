package com.turnin.domain.discover.presentation.route

import com.turnin.common.plugin.AuthenticatedRoute
import com.turnin.common.route.Api
import com.turnin.common.util.pagination.cursor.CursorPage
import com.turnin.common.util.pagination.cursor.getCursorPaginationParams
import com.turnin.common.util.pagination.cursor.toResponse
import com.turnin.common.validator.inputValidationAndReturn
import com.turnin.domain.discover.application.usecase.DiscoverUseCases
import com.turnin.domain.discover.presentation.dto.DiscoverContextResponse
import com.turnin.domain.discover.presentation.dto.toResponse
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import kotlin.text.toLongOrNull

fun AuthenticatedRoute.discoverRoutes(route: Api.V1.Discover, usecase: DiscoverUseCases) {
    route({
        tags = setOf(route.TAG)
        description = "Discover API"
    }) {
        get(route.ROUTE, { getDiscoverContextDocs() }) {
            val userId = call.queryParameters["userId"]
                ?.toLongOrNull()
                .inputValidationAndReturn("사용자 ID")
            val cursorPaginationParams = getCursorPaginationParams()
            val cursorPage = usecase.getDiscoverContext(
                userId = userId,
                cursor = cursorPaginationParams.cursor,
                pageSize = cursorPaginationParams.size,
            )
            val response = cursorPage.toResponse { discoverContextDto ->
                discoverContextDto.toResponse()
            }
            call.respond(HttpStatusCode.OK, response)
        }
    }
}

private fun RouteConfig.getDiscoverContextDocs() {
    summary = "사용자 ID로 탐색 컨텍스트(공유 키워드 정보) 목록 조회 (페이지네이션)"
    description = "사용자 ID로 탐색 컨텍스트(공유 키워드 정보) 목록을 커서 기반 페이지네이션으로 조회한다."
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
            description = "탐색 컨텍스트(공유 키워드 정보) 응답 바디"
            body<CursorPage<DiscoverContextResponse, Long>> {
                example("CursorPage(DiscoverContextResponse)") {
                    value = DiscoverContextResponse.sample
                }
            }
        }
    }
}
