package com.peekr.domain.auth.presentation.route

import com.peekr.common.api.Api
import com.peekr.common.exception.ErrorResponse
import com.peekr.domain.auth.application.usecase.AuthUseCase
import com.peekr.domain.auth.exception.AuthErrorCode
import com.peekr.domain.auth.presentation.dto.LoginRequest
import com.peekr.domain.auth.presentation.mapper.toDto
import com.peekr.domain.auth.presentation.mapper.toResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import kotlin.getValue
import org.koin.ktor.ext.inject

fun Route.authRoutes() {
    val authUseCase by inject<AuthUseCase>()

    post(Api.V1.Auth.LOGIN) {
        val request = call.receive<LoginRequest>()
        val token = authUseCase.login(request.toDto())
        if (token == null) {
            call.respond(AuthErrorResponse)
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

private val AuthErrorResponse = ErrorResponse(
    code = AuthErrorCode.LoginFailed.code,
    message = "로그인에 문제가 발생했습니다. (토큰 생성 실패)",
    status = HttpStatusCode.NotFound.value,
)
