package com.peekr.domain.user.presentation.route

import com.peekr.common.exception.ErrorResponse
import com.peekr.common.exception.toErrorResponse
import com.peekr.common.model.UserId
import com.peekr.common.plugin.AuthenticatedRoute
import com.peekr.common.route.Api
import com.peekr.common.route.Api.byPathParam
import com.peekr.common.validator.inputValidationAndReturn
import com.peekr.domain.user.application.usecase.UserUseCases
import com.peekr.domain.user.exception.UserErrorCode
import com.peekr.domain.user.presentation.dto.UserPatchRequest
import com.peekr.domain.user.presentation.dto.UserResponse
import com.peekr.domain.user.presentation.dto.toDto
import com.peekr.domain.user.presentation.dto.toResponse
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.patch
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond

// ------------------------------ Route ------------------------------
fun AuthenticatedRoute.userRoutes(route: Api.V1.User, userUseCases: UserUseCases) {
    route({
        tags = setOf(route.TAG)
        description = "User API"
    }) {
        get(route.ROUTE.byPathParam("id"), { getUserByIdDocs() }) {
            val userIdParam = call.pathParameters["id"]
                ?.toLongOrNull()
                .inputValidationAndReturn("사용자 ID")
            val userId = UserId(userIdParam)
            val user = userUseCases.get(userId)
            if (user != null) {
                call.respond(user.toResponse())
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    UserErrorCode.UserNotFound.toErrorResponse(HttpStatusCode.NotFound),
                )
            }
        }

        patch(route.ROUTE.byPathParam("id"), {}) {
            val userIdParam = call.pathParameters["id"]
                ?.toLongOrNull()
                .inputValidationAndReturn("사용자 ID")
            val userPatchRequest = call.receive<UserPatchRequest>()
            val userId = UserId(userIdParam)
            verifyAuthUserId(userId)
            val result = userUseCases.update(userId, userPatchRequest.toDto())
            if (result) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    UserErrorCode.UserNotFound.toErrorResponse(HttpStatusCode.NotFound),
                )
            }
        }
    }
}

// ------------------------------ Route Docs ------------------------------
fun RouteConfig.getUserByIdDocs() {
    summary = "사용자 조회"
    description = "사용자 ID로 사용자를 조회한다."
    request {
        pathParameter<Long>("id") {
            description = "사용자 ID 파라미터"
            example("Example") {
                value = 1
            }
        }
    }
    response {
        code(HttpStatusCode.OK) {
            body<UserResponse> {
                description = "사용자 정보"
                example("UserResponse") {
                    value = UserResponse.sample
                }
            }
        }
        code(HttpStatusCode.NotFound) {
            body<ErrorResponse> {
                description = "사용자가 존재하지 않는 경우"
                example("UserResponse") {
                    value = UserErrorCode.UserNotFound.toErrorResponse(HttpStatusCode.NotFound)
                }
            }
        }
    }
}

fun RouteConfig.patchUserDocs() {
    summary = "사용자 정보 수정"
    description = "사용자 정보를 수정한다."
    request {
        pathParameter<Long>("id") {
            description = "사용자 ID 파라미터"
            example("Example") {
                value = 1
            }
        }
        body<UserPatchRequest> {
            description = "사용자 정보 수정 요청 바디"
            example("Example") {
                value = UserPatchRequest.sample
            }
        }
    }
    response {
        code(HttpStatusCode.NoContent) {
            description = "사용자 정보 수정 성공 시"
        }
        code(HttpStatusCode.NotFound) {
            body<ErrorResponse> {
                description = "사용자가 존재하지 않는 경우"
                example("UserResponse") {
                    value = UserErrorCode.UserNotFound.toErrorResponse(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
