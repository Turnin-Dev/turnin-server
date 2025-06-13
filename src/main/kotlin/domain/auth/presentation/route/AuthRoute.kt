package com.peekr.domain.auth.presentation.route

import com.peekr.common.api.Api
import com.peekr.common.exception.ErrorResponse
import com.peekr.common.exception.toErrorResponse
import com.peekr.domain.auth.application.usecase.AuthUseCase
import com.peekr.domain.auth.domain.model.value.SocialLoginProvider
import com.peekr.domain.auth.exception.AuthErrorCode
import com.peekr.domain.auth.presentation.dto.LoginRequest
import com.peekr.domain.auth.presentation.dto.LoginResponse
import com.peekr.domain.auth.presentation.mapper.toDto
import com.peekr.domain.auth.presentation.mapper.toResponse
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlin.getValue
import org.koin.ktor.ext.inject

fun Route.authRoutes() {
    val authUseCase by inject<AuthUseCase>()

    post(Api.V1.Auth.LOGIN, { loginDocs() }) {
        val request = call.receive<LoginRequest>()
        val token = authUseCase.login(request.toDto())
        if (token == null) {
            call.respond(AuthErrorCode.LoginFailed.toErrorResponse(HttpStatusCode.BadRequest))
        } else {
            call.respond(token.toResponse())
        }
    }

    post(Api.V1.Auth.REGISTER) {
        val request = call.receive<LoginRequest>()
        val token = authUseCase.register(request.toDto())
        call.respond(token.toResponse())
    }
}

// ------------------------------ Route Docs ------------------------------
private fun RouteConfig.loginDocs() {
    description = "로그인(소셜)"
    request {
        body<LoginRequest> {
            description = "로그인 요청 바디"
            example("LoginRequest") {
                value = LoginRequest(
                    provider = SocialLoginProvider.Google,
                    providerId = "1231312312312",
                    name = "홍길동",
                    nickname = "길동이이이이",
                    profileImageUrl = "https://imageserver.com/13123123",
                    introduce = "안녕하세요!",
                )
            }
        }
    }
    response {
        code(HttpStatusCode.OK) {
            body<LoginResponse> {
                description = "로그인 응답 바디 (JWT 토큰)"
                example("LoginResponse") {
                    value = LoginResponse(
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
