package com.peekr.domain.keyword.presentation.route

import com.peekr.common.api.Api
import com.peekr.common.api.Api.byPathParam
import com.peekr.common.exception.CommonErrorCode
import com.peekr.common.jwt.JWTValidator.compareAuthUserIdAndMyUserId
import com.peekr.common.validator.ValidatorException
import com.peekr.domain.core.model.UserId
import com.peekr.domain.keyword.application.dto.UserKeywordIdDto
import com.peekr.domain.keyword.application.usecase.UserKeywordUseCase
import com.peekr.domain.keyword.presentation.dto.AddUserKeywordRequest
import com.peekr.domain.keyword.presentation.dto.PatchUserKeywordRequest
import com.peekr.domain.keyword.presentation.dto.toDto
import com.peekr.domain.keyword.presentation.dto.toResponse
import com.peekr.domain.keyword.presentation.validation.validate
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
            val userIdParam = call.pathParameters["userId"]?.toLongOrNull()
                ?: throw ValidatorException(CommonErrorCode.MalformedRequest.description)
            val userId = UserId(userIdParam)
            compareAuthUserIdAndMyUserId(userId)
            val userKeywords = userKeywordUseCase.getListById(userId)
            call.respond(userKeywords.toResponse())
        }

        post(route.ROUTE, { }) {
            val addUserKeywordRequest = call.receive<AddUserKeywordRequest>()
            addUserKeywordRequest.validate()
            val ownerId = UserId(addUserKeywordRequest.userId)
            compareAuthUserIdAndMyUserId(ownerId)
            val addUserKeywordRequestDto = addUserKeywordRequest.toDto().copy(userId = ownerId)
            val userKeywordDto = userKeywordUseCase.add(addUserKeywordRequestDto)
            call.respond(userKeywordDto.toResponse())
        }

        patch(route.ROUTE, { }) {
            val ownerIdParam = call.queryParameters["ownerId"]?.toLongOrNull()
                ?: throw ValidatorException(CommonErrorCode.MalformedRequest.description)
            val userKeywordIdParam = call.queryParameters["userKeywordId"]?.toLongOrNull()
                ?: throw ValidatorException(CommonErrorCode.MalformedRequest.description)
            val ownerId = UserId(ownerIdParam)
            compareAuthUserIdAndMyUserId(ownerId)
            val userKeywordIdDto = UserKeywordIdDto(userKeywordIdParam)
            val patchUserKeywordRequest = call.receive<PatchUserKeywordRequest>()
            val result = userKeywordUseCase.update(
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
            compareAuthUserIdAndMyUserId(ownerId)
            val userKeywordIdDto = UserKeywordIdDto(userKeywordIdParam)
            val result = userKeywordUseCase.delete(ownerId, userKeywordIdDto)
            call.respond(HttpStatusCode.OK, result)
        }
    }
}
