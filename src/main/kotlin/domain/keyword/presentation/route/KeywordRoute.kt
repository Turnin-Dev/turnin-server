package com.peekr.domain.keyword.presentation.route

import com.peekr.common.api.Api
import com.peekr.common.api.Api.byPathParam
import com.peekr.common.jwt.JWTValidator.getTokenUserID
import com.peekr.common.jwt.exception.TokenException
import com.peekr.common.validator.ValidatorException
import com.peekr.domain.core.model.UserId
import com.peekr.domain.keyword.application.usecase.UserKeywordUseCase
import com.peekr.domain.keyword.presentation.dto.toResponse
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
            val authUserIdParam = getTokenUserID()
            val authUserId = UserId.from(authUserIdParam)
            val userIdParam = call.pathParameters["userId"]
            val userId = try {
                UserId.from(userIdParam)
            } catch (e: IllegalArgumentException) {
                throw ValidatorException(e.message)
            }

            if (authUserId.id != userId.id) {
                throw TokenException.UnauthorizedUserException()
            }

            val userKeywords = userKeywordUseCase.getListById(userId)
            call.respond(userKeywords.toResponse())
        }
    }
}
