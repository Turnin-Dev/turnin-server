package com.peekr.domain.keyword.presentation.route

import com.peekr.common.api.Api
import com.peekr.common.api.Api.byPathParam
import com.peekr.common.jwt.JWTValidator.getValidatedMyUserId
import com.peekr.domain.keyword.application.dto.UserKeywordIdDto
import com.peekr.domain.keyword.application.usecase.UserKeywordUseCase
import com.peekr.domain.keyword.presentation.dto.AddUserKeywordRequest
import com.peekr.domain.keyword.presentation.dto.PatchUserKeywordRequest
import com.peekr.domain.keyword.presentation.dto.toDto
import com.peekr.domain.keyword.presentation.dto.toResponse
import com.peekr.domain.keyword.presentation.dto.validate
import com.peekr.domain.keyword.presentation.validation.validateUserKeywordIdAndReturn
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.patch
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

fun Route.keywordRoutes(route: Api.V1.Keyword, userKeywordUseCase: UserKeywordUseCase) {
    route({
        tags = setOf(route.TAG)
        description = "Keyword API"
    }) {
        get(route.ROUTE.byPathParam("userId"), { }) {
            val userIdParam = call.pathParameters["userId"]
            val userId = getValidatedMyUserId(userIdParam)
            val userKeywords = userKeywordUseCase.getListById(userId)
            call.respond(userKeywords.toResponse())
        }

        post(route.ROUTE, { }) {
            val addUserKeywordRequest = call.receive<AddUserKeywordRequest>()
            addUserKeywordRequest.validate()
            val userKeywordDto = userKeywordUseCase.add(addUserKeywordRequest.toDto())
            call.respond(userKeywordDto.toResponse())
        }

        patch(route.ROUTE.byPathParam("userKeywordId"), { }) {
            val userKeywordIdParam = call.pathParameters["userKeywordId"]
            val userKeywordId = userKeywordIdParam.validateUserKeywordIdAndReturn()
            val userKeywordIdDto = UserKeywordIdDto(userKeywordId)
            val patchUserKeywordRequest = call.receive<PatchUserKeywordRequest>()
            val result = userKeywordUseCase.update(
                userKeywordId = userKeywordIdDto,
                patch = patchUserKeywordRequest.toDto(),
            )
            call.respond(HttpStatusCode.OK, result)
        }

        delete(route.ROUTE.byPathParam("userKeywordId"), { }) {
            val userKeywordIdParam = call.pathParameters["userKeywordId"]
            val userKeywordId = userKeywordIdParam.validateUserKeywordIdAndReturn()
            val userKeywordIdDto = UserKeywordIdDto(userKeywordId)
            val result = userKeywordUseCase.delete(userKeywordIdDto)
            call.respond(HttpStatusCode.OK, result)
        }
    }
}
