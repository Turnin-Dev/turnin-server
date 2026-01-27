package com.peekr.domain.feed.presentation.route

import com.peekr.common.plugin.AuthenticatedRoute
import com.peekr.common.route.Api
import com.peekr.common.util.pagination.cursor.CursorPage
import com.peekr.common.util.pagination.cursor.getFeedCursorPaginationParams
import com.peekr.common.util.pagination.cursor.toResponse
import com.peekr.domain.feed.application.dto.FeedCursor
import com.peekr.domain.feed.application.usecase.FeedUseCases
import com.peekr.domain.feed.presentation.dto.FeedResponse
import com.peekr.domain.feed.presentation.dto.toResponse
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond

fun AuthenticatedRoute.feedRoutes(route: Api.V1.Feed, usecase: FeedUseCases) {
    route(route.ROUTE, {
        tags = setOf(route.TAG)
        description = "Feed API"
    }) {
        get({ getFeedsDocs() }) {
            val userId = extractUserIdWithToken()
            val feedCursorParams = getFeedCursorPaginationParams()
            val cursorPage = usecase.getFeeds(
                userId = userId.value,
                cursor = feedCursorParams.cursor,
                pageSize = feedCursorParams.size,
            )
            val response = cursorPage.toResponse { feedDto ->
                feedDto.toResponse()
            }
            call.respond(HttpStatusCode.OK, response)
        }
    }
}

private fun RouteConfig.getFeedsDocs() {
    summary = "피드 조회"
    description = "피드를 조회한다. 커서 페이지네이션을 사용한다."
    request {
        queryParameter<Double>("cursorScore") {
            description = "커서 값 1 (피드 점수)"
        }
        queryParameter<Long>("cursorCreatedAt") {
            description = "커서 값 2 (피드 생성일자)"
        }
        queryParameter<Long>("cursorUserKeywordId") {
            description = "커서 값 3 (피드의 사용자 키워드 ID)"
        }
    }
    response {
        code(HttpStatusCode.OK) {
            description = "피드 조회 응답 바디"
            body<CursorPage<FeedResponse, FeedCursor>> {
                example("다음 페이지가 존재하는 경우") {
                    value = FeedResponse.sample
                }
                example("다음 페이지가 존재하지 않는 경우") {
                    value = FeedResponse.sample.copy(nextCursor = null)
                }
            }
        }
    }
}
