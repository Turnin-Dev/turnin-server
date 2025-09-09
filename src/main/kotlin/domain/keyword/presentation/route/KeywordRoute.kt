package com.peekr.domain.keyword.presentation.route

import com.peekr.common.api.Api
import com.peekr.common.api.Api.byPathParam
import com.peekr.domain.keyword.application.usecase.UserKeywordUseCase
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

fun Route.keywordRoutes(route: Api.V1.Keyword, userKeywordUseCase: UserKeywordUseCase) {
    route({
        tags = setOf(route.TAG)
        description = "Keyword API"
    }) {
        get(route.ROUTE.byPathParam("userId"), { }) {
            call.respond("Hello World!")
//            val userIdParam = call.pathParameters["userId"]
//            val userId = UserId.from(userIdParam)
//            val userKeywords = userKeywordUseCase.getListById(userId)
//            call.respond(userKeywords.toResponse())
        }
    }
}
