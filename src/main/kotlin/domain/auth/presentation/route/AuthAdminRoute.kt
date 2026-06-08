package com.turnin.domain.auth.presentation.route

import com.turnin.common.exception.ErrorResponse
import com.turnin.common.exception.toErrorResponse
import com.turnin.common.model.id.DisplayId
import com.turnin.common.route.Api
import com.turnin.domain.auth.application.usecase.AuthAdminUseCases
import com.turnin.domain.auth.exception.AuthErrorCode
import com.turnin.domain.auth.presentation.dto.AdminRegisterRequest
import com.turnin.domain.auth.presentation.dto.RegisterResultResponse
import com.turnin.domain.auth.presentation.dto.toDto
import com.turnin.domain.auth.presentation.dto.toRegisterRequest
import com.turnin.domain.auth.presentation.dto.toResponse
import com.turnin.domain.auth.presentation.dto.validate
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

fun Route.authAdminRoutes(route: Api.Admin.Auth, usecase: AuthAdminUseCases) {
    route(route.ROUTE, {
        tags = setOf(route.TAG)
        description = "Auth Admin API"
        specName = "admin"
    }) {
        post(route.REGISTER, { registerDocs() }) {
            val originalRequest = call.receive<AdminRegisterRequest>()
            usecase.validateAdminSecretKey(originalRequest.secretKey)

            val request = originalRequest.toRegisterRequest()
            request.validate()

            val displayId = DisplayId(request.displayId)
            val existsByDisplayId = usecase.existsDisplayId(displayId)

            if (!existsByDisplayId) {
                val registerResultDto = usecase.register(request.toDto())
                call.respond(HttpStatusCode.Created, registerResultDto.toResponse())
            } else {
                call.respond(
                    HttpStatusCode.Conflict,
                    AuthErrorCode.UserDuplicated.toErrorResponse(HttpStatusCode.Conflict),
                )
            }
        }
    }
}

private fun RouteConfig.registerDocs() {
    summary = "회원가입 (관리자용)"
    description = "회원가입 (관리자용)"
    request {
        body<AdminRegisterRequest> {
            description = "회원가입 요청 본문"
            example("AdminRegisterRequest") {
                value = AdminRegisterRequest.sample
            }
        }
    }
    response {
        code(HttpStatusCode.Created) {
            body<RegisterResultResponse> {
                description = "회원가입 응답 본문 (사용자 ID + JWT 토큰)"
                example("RegisterResultResponse") {
                    value = RegisterResultResponse.sample
                }
            }
        }
        code(HttpStatusCode.Conflict) {
            body<ErrorResponse> {
                description = "DisplayID 중복"
                example("UserDuplicated") {
                    value = AuthErrorCode.UserDuplicated.toErrorResponse(HttpStatusCode.Conflict)
                }
            }
        }
    }
}
