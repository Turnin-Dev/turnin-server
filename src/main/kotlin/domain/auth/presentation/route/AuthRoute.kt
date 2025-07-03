package com.peekr.domain.auth.presentation.route

import com.peekr.common.api.Api
import com.peekr.common.api.Api.byId
import com.peekr.common.exception.CommonErrorCode
import com.peekr.common.exception.ErrorResponse
import com.peekr.common.exception.toErrorResponse
import com.peekr.common.jwt.domain.model.entity.JWTToken
import com.peekr.common.validator.CommonValidator.userIdValidatorAndReturn
import com.peekr.domain.auth.application.usecase.AuthUseCase
import com.peekr.domain.auth.exception.AuthErrorCode
import com.peekr.domain.auth.presentation.dto.JWTTokenResponse
import com.peekr.domain.auth.presentation.dto.LoginRequest
import com.peekr.domain.auth.presentation.dto.RegisterRequest
import com.peekr.domain.auth.presentation.mapper.toDto
import com.peekr.domain.auth.presentation.mapper.toResponse
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

// ------------------------------ Route ------------------------------
fun Route.authRoutes(route: Api.V1.Auth, authUseCase: AuthUseCase) {
    route(route.ROUTE, {
        tags = setOf(route.TAG)
        description = "Auth API"
    }) {
        post(route.LOGIN, { loginDocs() }) {
            val request = call.receive<LoginRequest>()
            request.validate()
            val token = authUseCase.login(request.toDto())
            if (token == null) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    AuthErrorCode.LoginFailed.toErrorResponse(HttpStatusCode.BadRequest),
                )
            } else {
                call.respond(token.toResponse())
            }
        }

        post(route.REGISTER, { registerDocs() }) {
            val request = call.receive<RegisterRequest>()
            request.validate()
            val token = authUseCase.register(request.toDto())
            call.respond(HttpStatusCode.Created, token.toResponse())
        }

        get(route.REFRESH.byId("id"), { refreshDocs() }) {
            val refreshToken = call.request.headers["Authorization"]
            val userIdParam = call.pathParameters["id"]
            val userId = userIdValidatorAndReturn(userIdParam)
            refreshToken?.let {
                JWTToken.validate(refreshToken)
                val token = authUseCase.refresh(userId, refreshToken)
                if (token == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        AuthErrorCode.RefreshTokenExpired.toErrorResponse(HttpStatusCode.Unauthorized),
                    )
                } else {
                    call.respond(token.toResponse())
                }
            } ?: call.respond(
                HttpStatusCode.BadRequest,
                CommonErrorCode.EmptyRequestHeader.toErrorResponse(HttpStatusCode.BadRequest),
            )
        }
    }
}

// ------------------------------ Route Docs ------------------------------
private fun RouteConfig.loginDocs() {
    summary = "소셜 로그인"
    description = "소셜 로그인"
    request {
        body<LoginRequest> {
            description = "로그인 요청 본문"
            example("LoginRequest") {
                value = LoginRequest.sample
            }
        }
    }
    response {
        code(HttpStatusCode.OK) {
            body<JWTTokenResponse> {
                description = "로그인 응답 본문 (JWT 토큰)"
                example("JWTTokenResponse") {
                    value = JWTTokenResponse.sample
                }
            }
        }
        default {
            body<ErrorResponse> {
                example("ErrorResponse") {
                    value = ErrorResponse(
                        code = AuthErrorCode.LoginFailed.code,
                        message = "Login failed",
                        status = HttpStatusCode.BadRequest.value,
                    )
                }
            }
        }
    }
}

private fun RouteConfig.registerDocs() {
    summary = "회원가입"
    description = "회원가입"
    request {
        body<RegisterRequest> {
            description = "회원가입 요청 본문"
            example("RegisterRequest") {
                value = RegisterRequest.sample
            }
        }
    }
    response {
        code(HttpStatusCode.Created) {
            body<JWTTokenResponse> {
                description = "회원가입 응답 본문 (JWT 토큰)"
                example("JWTTokenResponse") {
                    value = JWTTokenResponse.sample
                }
            }
        }
        default {
            body<ErrorResponse> {
                example("ErrorResponse") {
                    value = ErrorResponse(
                        code = AuthErrorCode.UserDuplicated.code,
                        message = "Register failed",
                        status = HttpStatusCode.Conflict.value,
                    )
                }
            }
        }
    }
}

private fun RouteConfig.refreshDocs() {
    summary = "리프레쉬 토큰 갱신"
    description = "리프레쉬 토큰 갱신 요청"
    request {
        pathParameter<Long>("id") {
            description = "사용자 ID 파라미터"
            example("Example") {
                value = 1
            }
        }
        headerParameter<String>("Authorization") {
            description = "리프레쉬 토큰"
            example("Example") {
                value = "Bearer aaa.bbb.ccc"
            }
        }
    }
    response {
        code(HttpStatusCode.OK) {
            body<JWTTokenResponse> {
                example("JWTTokenResponse") {
                    value = JWTTokenResponse.sample
                }
            }
        }
        code(HttpStatusCode.Unauthorized) {
            body<ErrorResponse> {
                example("ErrorResponse") {
                    value = AuthErrorCode.RefreshTokenExpired.toErrorResponse(HttpStatusCode.Unauthorized)
                }
            }
        }
        code(HttpStatusCode.BadRequest) {
            body<ErrorResponse> {
                example("ErrorResponse") {
                    value = CommonErrorCode.EmptyRequestHeader.toErrorResponse(HttpStatusCode.BadRequest)
                }
            }
        }
    }
}
