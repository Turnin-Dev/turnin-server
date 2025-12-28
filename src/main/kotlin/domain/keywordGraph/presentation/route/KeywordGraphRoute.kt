package com.peekr.domain.keywordGraph.presentation.route

import com.peekr.common.plugin.AuthenticatedRoute
import com.peekr.common.route.Api
import com.peekr.common.util.pagination.cursor.getCursorPaginationParams
import com.peekr.common.util.pagination.cursor.toResponse
import com.peekr.common.validator.inputValidationAndReturn
import com.peekr.domain.keywordGraph.application.usecase.KeywordGraphUseCases
import com.peekr.domain.keywordGraph.presentation.dto.toResponse
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond

fun AuthenticatedRoute.keywordGraphRoutes(route: Api.V1.KeywordGraph, usecase: KeywordGraphUseCases) {
    route(route.ROUTE, {
        tags = setOf(route.TAG)
        description = "Keyword Graph API"
    }) {
        get(route.ROUTE, { }) {
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
