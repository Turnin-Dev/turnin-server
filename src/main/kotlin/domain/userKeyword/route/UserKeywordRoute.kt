package com.peekr.domain.userKeyword.route

import com.peekr.common.api.Api
import com.peekr.common.api.Api.byPathParam
import com.peekr.common.exception.CommonErrorCode
import com.peekr.common.jwt.JWTValidator.verifyAuthUserId
import com.peekr.common.validator.ValidatorException
import com.peekr.domain.core.model.UserId
import com.peekr.domain.keyword.presentation.dto.CreateUserKeywordRequest
import com.peekr.domain.keyword.presentation.dto.PatchUserKeywordRequest
import com.peekr.domain.keyword.presentation.dto.toDto
import com.peekr.domain.keyword.presentation.dto.toResponse
import com.peekr.domain.userKeyword.application.dto.UserKeywordIdDto
import com.peekr.domain.userKeyword.application.usecase.UserKeywordUseCase
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.patch
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

fun Route.userKeywordRoutes(route: Api.V1.UserKeyword, usecase: UserKeywordUseCase) {
    route({
        tags = setOf(route.TAG)
        description = "User Keyword API"
    }) {
        get(route.ROUTE.byPathParam("userId"), { }) {
            val userIdParam = call.pathParameters["userId"]?.toLongOrNull()
                ?: throw ValidatorException(CommonErrorCode.MalformedRequest.description)
            val userId = UserId(userIdParam)
            verifyAuthUserId(userId)
            val userKeywords = usecase.getListById(userId)
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
            val ownerIdParam = call.queryParameters["ownerId"]?.toLongOrNull()
                ?: throw ValidatorException(CommonErrorCode.MalformedRequest.description)
            val userKeywordIdParam = call.queryParameters["userKeywordId"]?.toLongOrNull()
                ?: throw ValidatorException(CommonErrorCode.MalformedRequest.description)
            val ownerId = UserId(ownerIdParam)
            verifyAuthUserId(ownerId)
            val userKeywordIdDto = UserKeywordIdDto(userKeywordIdParam)
            val patchUserKeywordRequest = call.receive<PatchUserKeywordRequest>()
            val result = usecase.update(
                ownerId = ownerId,
                userKeywordId = userKeywordIdDto,
                patch = patchUserKeywordRequest.toDto(),
            )
            call.respond(HttpStatusCode.OK, result)
        }

        delete(route.ROUTE, { }) {
            val ownerIdParam = call.queryParameters["ownerId"]?.toLongOrNull()
                ?: throw ValidatorException(CommonErrorCode.MalformedRequest.description)
            val userKeywordIdParam = call.queryParameters["userKeywordId"]?.toLongOrNull()
                ?: throw ValidatorException(CommonErrorCode.MalformedRequest.description)
            val ownerId = UserId(ownerIdParam)
            verifyAuthUserId(ownerId)
            val userKeywordIdDto = UserKeywordIdDto(userKeywordIdParam)
            val result = usecase.delete(ownerId, userKeywordIdDto)
            call.respond(HttpStatusCode.OK, result)
        }
    }
}
