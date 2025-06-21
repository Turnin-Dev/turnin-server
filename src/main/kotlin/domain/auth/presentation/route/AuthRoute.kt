package com.peekr.domain.auth.presentation.route

import com.peekr.common.api.Api
import com.peekr.common.exception.ErrorResponse
import com.peekr.common.exception.toErrorResponse
import com.peekr.domain.auth.application.usecase.AuthUseCase
import com.peekr.domain.auth.domain.model.SocialLoginProvider
import com.peekr.domain.auth.exception.AuthErrorCode
import com.peekr.domain.auth.presentation.dto.JWTTokenResponse
import com.peekr.domain.auth.presentation.dto.LoginRequest
import com.peekr.domain.auth.presentation.dto.RegisterRequest
import com.peekr.domain.auth.presentation.mapper.toDto
import com.peekr.domain.auth.presentation.mapper.toResponse
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import org.koin.ktor.ext.inject

// ------------------------------ Route ------------------------------
fun Route.authRoutes(route: Api.V1.Auth, authUseCaseParam: AuthUseCase? = null) {
    val authUseCase by lazy {
        authUseCaseParam ?: inject<AuthUseCase>().value
    }

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
                value = LoginRequest(
                    provider = SocialLoginProvider.Google.name,
                    providerId = "1231312312312",
                )
            }
        }
    }
    response {
        code(HttpStatusCode.OK) {
            body<JWTTokenResponse> {
                description = "로그인 응답 본문 (JWT 토큰)"
                example("JWTTokenResponse") {
                    value = JWTTokenResponse(
                        accessToken = "aaa.bbb.ccc",
                        refreshToken = "aaa.bbb.ccc",
                    )
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
                value = RegisterRequest(
                    provider = SocialLoginProvider.Google.name,
                    providerId = "providerIDDDDD",
                    name = "honggd",
                    nickname = "honggggg",
                    profileImageUrl = "http://example.com/!@#$%^&*/profile.jpg",
                    introduce = "Hello!",
                )
            }
        }
    }
    response {
        code(HttpStatusCode.Created) {
            body<JWTTokenResponse> {
                description = "회원가입 응답 본문 (JWT 토큰)"
                example("JWTTokenResponse") {
                    value = JWTTokenResponse(
                        accessToken = "aaa.bbb.ccc",
                        refreshToken = "aaa.bbb.ccc",
                    )
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
