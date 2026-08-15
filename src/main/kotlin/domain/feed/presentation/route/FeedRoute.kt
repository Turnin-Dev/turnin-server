package com.turnin.domain.feed.presentation.route

import com.turnin.common.plugin.AuthenticatedRoute
import com.turnin.common.route.Api
import com.turnin.common.util.pagination.cursor.CursorPage
import com.turnin.common.util.pagination.cursor.getStringCursorPaginationParams
import com.turnin.common.util.pagination.cursor.toResponse
import com.turnin.domain.feed.application.dto.FeedType
import com.turnin.domain.feed.application.usecase.FeedUseCases
import com.turnin.domain.feed.presentation.dto.FeedResponse
import com.turnin.domain.feed.presentation.dto.toResponse
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
            val feedType = FeedType.valueOf(
                call.queryParameters["feed_type"] ?: FeedType.ALL.name,
            )
            val feedCursorParams = getStringCursorPaginationParams()

            val page = usecase.getFeeds(feedType, userId, feedCursorParams.cursor, feedCursorParams.size)

            call.respond(HttpStatusCode.OK, page.toResponse { feedDto -> feedDto.toResponse() })
        }
    }
}

private fun RouteConfig.getFeedsDocs() {
    summary = "피드 조회"
    description = "피드 유형에 따라 피드를 조회한다. 커서 페이지네이션을 사용한다."
    request {
        queryParameter<String>("feed_type") {
            description = "피드 유형 (ALL / FRIEND 등)"
            required = false
        }
        queryParameter<String>("cursor") {
            description = "다음 페이지 조회를 위한 커서. 이전 응답의 nextCursor 값을 그대로 전달하며, 첫 페이지 조회 시에는 생략한다."
            required = false
        }
        queryParameter<Int>("size") {
            description = "페이지네이션에 필요한 페이지 크기"
        }
    }
    response {
        code(HttpStatusCode.OK) {
            description = "피드 조회 응답 바디"
            body<CursorPage<FeedResponse, String>> {
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
