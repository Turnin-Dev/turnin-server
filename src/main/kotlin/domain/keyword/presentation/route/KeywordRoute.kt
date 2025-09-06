package com.peekr.domain.keyword.presentation.route

import com.peekr.common.api.Api
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.server.routing.Route

fun Route.keywordRoutes(route: Api.V1.Keyword) {
    route(route.ROUTE, {
        tags = setOf(route.TAG)
        description = "Keyword API"
    }) {
        get(route.ROUTE, { }) {
        }
    }
}
