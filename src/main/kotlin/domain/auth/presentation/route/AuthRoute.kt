package com.peekr.domain.auth.presentation.route

import com.peekr.common.presentation.exception.ApiException
import com.peekr.domain.auth.application.usecase.AuthUseCase
import com.peekr.domain.auth.presentation.dto.LoginRequest
import com.peekr.domain.auth.presentation.mapper.toDto
import com.peekr.domain.auth.presentation.mapper.toResponse
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import kotlin.getValue
import org.koin.ktor.ext.inject

fun Route.authRoutes() {
    val authUseCase by inject<AuthUseCase>()

    route("/auth") {
        post("/login") {
            val request = call.receive<LoginRequest>()
            val token = authUseCase.login(request.toDto())
            if (token == null) {
                throw ApiException(
                    code = "",
                    message = "사용자가 존재하지 않습니다.",
                    status = HttpStatusCode.NotFound,
                )
            }
            call.respond(token.toResponse())
        }

        post("/register") {
            val request = call.receive<LoginRequest>()
            val token = authUseCase.register(request.toDto())
            call.respond(token.toResponse())
        }
    }
}
