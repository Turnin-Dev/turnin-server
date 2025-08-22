package com.peekr.domain.user.presentation.route

import com.peekr.common.api.Api
import com.peekr.common.api.Api.byPathParam
import com.peekr.common.exception.ErrorResponse
import com.peekr.common.exception.toErrorResponse
import com.peekr.common.validator.CommonValidator.validationUserIdAndReturn
import com.peekr.domain.user.application.usecase.UserUseCase
import com.peekr.domain.user.exception.UserErrorCode
import com.peekr.domain.user.presentation.dto.UserResponse
import com.peekr.domain.user.presentation.dto.toResponse
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

// ------------------------------ Route ------------------------------
fun Route.userRoutes(route: Api.V1.User, userUseCase: UserUseCase) {
    route(route.ROUTE, {
        tags = setOf(route.TAG)
        description = "User API"
    }) {
        get(route.ROUTE.byPathParam("id"), { getUserByIdDocs() }) {
            val userIdParam = call.pathParameters["id"]
            val userId = validationUserIdAndReturn(userIdParam)
            val user = userUseCase.getUserById(userId)
            if (user != null) {
                call.respond(user.toResponse())
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
