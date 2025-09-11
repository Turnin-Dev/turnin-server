package com.peekr.domain.userKeyword.presentation.route

import com.peekr.common.api.Api
import com.peekr.common.api.Api.byPathParam
import com.peekr.common.jwt.JWTValidator.verifyAuthUserId
import com.peekr.domain.core.model.UserId
import com.peekr.domain.core.model.UserKeywordId
import com.peekr.domain.core.validator.inputValidationAndReturn
import com.peekr.domain.userKeyword.application.usecase.UserKeywordUseCases
import com.peekr.domain.userKeyword.presentation.dto.CreateUserKeywordRequest
import com.peekr.domain.userKeyword.presentation.dto.PatchUserKeywordRequest
import com.peekr.domain.userKeyword.presentation.dto.toDto
import com.peekr.domain.userKeyword.presentation.dto.toResponse
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.patch
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

fun Route.userKeywordRoutes(route: Api.V1.UserKeyword, usecase: UserKeywordUseCases) {
    route({
        tags = setOf(route.TAG)
        description = "User Keyword API"
    }) {
        get(route.ROUTE.byPathParam("userId"), { }) {
            val userIdParam = call.pathParameters["userId"]
                ?.toLongOrNull()
                .inputValidationAndReturn("사용자 ID")
            val userId = UserId(userIdParam)
            verifyAuthUserId(userId)
            val userKeywords = usecase.get(userId)
            call.respond(userKeywords.toResponse())
        }

        post(route.ROUTE, { }) {
            val createUserKeywordRequest = call.receive<CreateUserKeywordRequest>()
            val ownerId = UserId(createUserKeywordRequest.userId)
            verifyAuthUserId(ownerId)
            val addUserKeywordRequestDto = createUserKeywordRequest.toDto().copy(userId = ownerId)
            val userKeywordDto = usecase.create(addUserKeywordRequestDto)
            call.respond(userKeywordDto.toResponse())
        }

        patch(route.ROUTE, { }) {
            val ownerIdParam = call.queryParameters["ownerId"]
                ?.toLongOrNull()
                .inputValidationAndReturn("소유자 ID")
            val userKeywordIdParam = call.queryParameters["userKeywordId"]
                ?.toLongOrNull()
                .inputValidationAndReturn("사용자 키워드 ID")
            val ownerId = UserId(ownerIdParam)
            verifyAuthUserId(ownerId)
            val userKeywordId = UserKeywordId(userKeywordIdParam)
            val patchUserKeywordRequest = call.receive<PatchUserKeywordRequest>()
            val result = usecase.update(
                ownerId = ownerId,
                userKeywordId = userKeywordId,
                patch = patchUserKeywordRequest.toDto(),
            )
            call.respond(HttpStatusCode.OK, result)
        }

        delete(route.ROUTE, { }) {
            val ownerIdParam = call.queryParameters["ownerId"]
                ?.toLongOrNull()
                .inputValidationAndReturn("소유자 ID")
            val userKeywordIdParam = call.queryParameters["userKeywordId"]
                ?.toLongOrNull()
                .inputValidationAndReturn("사용자 키워드 ID")
            val ownerId = UserId(ownerIdParam)
            verifyAuthUserId(ownerId)
            val userKeywordId = UserKeywordId(userKeywordIdParam)
            val result = usecase.delete(ownerId, userKeywordId)
            call.respond(HttpStatusCode.OK, result)
        }
    }
}
