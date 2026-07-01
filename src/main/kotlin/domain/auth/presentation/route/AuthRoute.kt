package com.turnin.domain.auth.presentation.route

import com.turnin.common.exception.ErrorResponse
import com.turnin.common.exception.common.CommonErrorCode
import com.turnin.common.exception.toErrorResponse
import com.turnin.common.jwt.JWTValidator
import com.turnin.common.jwt.domain.model.JWTToken.Companion.removeBearerHeader
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.id.DisplayId
import com.turnin.common.route.Api
import com.turnin.common.route.Api.byPathParam
import com.turnin.common.validator.inputValidationAndReturn
import com.turnin.domain.auth.application.usecase.AuthUseCases
import com.turnin.domain.auth.exception.AuthErrorCode
import com.turnin.domain.auth.presentation.dto.ExistsResultResponse
import com.turnin.domain.auth.presentation.dto.JWTTokenResponse
import com.turnin.domain.auth.presentation.dto.LoginRequest
import com.turnin.domain.auth.presentation.dto.LoginResultResponse
import com.turnin.domain.auth.presentation.dto.RegisterRequest
import com.turnin.domain.auth.presentation.dto.RegisterResultResponse
import com.turnin.domain.auth.presentation.dto.toDto
import com.turnin.domain.auth.presentation.dto.toResponse
import com.turnin.domain.auth.presentation.dto.validate
import com.turnin.domain.auth.presentation.validation.validateDisplayId
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

// ------------------------------ Route ------------------------------
fun Route.authRoutes(route: Api.V1.Auth, usecase: AuthUseCases) {
    route(route.ROUTE, {
        tags = setOf(route.TAG)
        description = "Auth API"
    }) {
        post(route.LOGIN, { loginDocs() }) {
            val request = call.receive<LoginRequest>()
            request.validate()
            val loginResultDto = usecase.login(request.toDto())
            call.respond(loginResultDto.toResponse())
        }

        post(route.REGISTER, { registerDocs() }) {
            val request = call.receive<RegisterRequest>()
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

        get(route.REFRESH, { refreshDocs() }) {
            val refreshTokenParam = call.request.headers["Authorization"].inputValidationAndReturn("인증 토큰")
            JWTValidator.validate(refreshTokenParam)
            val refreshToken = refreshTokenParam.removeBearerHeader()
            val token = usecase.refresh(refreshToken)
            if (token == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    AuthErrorCode.RefreshTokenExpired.toErrorResponse(HttpStatusCode.Unauthorized),
                )
            } else {
                call.respond(token.toResponse())
            }
        }

        get(route.EXISTS_USER.byPathParam("provider", "providerId"), { findUserDocs() }) {
            val provider = call.request.pathVariables["provider"] ?: return@get
            val providerId = call.request.pathVariables["providerId"] ?: return@get

            try {
                val findUserResultDto = usecase.findUser(
                    provider = SocialLoginProvider.valueOf(provider.trim().uppercase()),
                    providerId = providerId.trim(),
                )
                call.respond(findUserResultDto.toResponse())
            } catch (e: IllegalArgumentException) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    AuthErrorCode
                        .PathParameterInvalid("provider")
                        .toErrorResponse(HttpStatusCode.BadRequest),
                )
            }
        }

        get(route.EXISTS_DISPLAY_ID.byPathParam("displayId"), { existsDisplayIdDocs() }) {
            val displayIdParam = call.request.pathVariables["displayId"]
            displayIdParam?.validateDisplayId()
            if (displayIdParam.isNullOrBlank()) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    CommonErrorCode.ValidationDefault.toErrorResponse(HttpStatusCode.BadRequest),
                )
                return@get
            }

            val displayId = DisplayId(displayIdParam.trim())
            val existsDisplayId = usecase.existsDisplayId(displayId)
            call.respond(
                HttpStatusCode.OK,
                ExistsResultResponse(exists = existsDisplayId),
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
            body<LoginResultResponse> {
                description = "로그인 응답 본문 (사용자 ID + JWT 토큰)"
                example("LoginResultResponse") {
                    value = LoginResultResponse.sample
                }
            }
        }
        default {
            body<ErrorResponse> {
                example("ErrorResponse") {
                    value = ErrorResponse(
                        code = AuthErrorCode.LoginFailed.code,
                        message = "Login failed",
                        status = HttpStatusCode.NotFound.value,
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

private fun RouteConfig.refreshDocs() {
    summary = "리프레쉬 토큰 갱신"
    description = "리프레쉬 토큰 갱신 요청"
    request {
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

private fun RouteConfig.findUserDocs() {
    summary = "사용자 찾기"
    description = "사용자 존재 여부 확인"
    request {
        pathParameter<String>("provider") {
            description = "소셜로그인 플랫폼 (대문자 형식)"
            example("google") {
                value = "GOOGLE"
            }
        }
        pathParameter<String>("providerId") {
            description = "소셜로그인 플랫폼에서 제공하는 ID"
            example("google") {
                value = "129387"
            }
        }
    }

    response {
        code(HttpStatusCode.OK) {
            body<ExistsResultResponse> {
                example("FindUserResultResponse") {
                    value = """
                        {
                            "exists": true
                        }
                    """.trimIndent()
                }
            }
        }

        code(HttpStatusCode.BadRequest) {
            body<ErrorResponse> {
                example("PathParameterInvalid") {
                    value = AuthErrorCode
                        .PathParameterInvalid("provider")
                        .toErrorResponse(HttpStatusCode.BadRequest)
                }
                example("ValidationError") {
                    value = CommonErrorCode.ValidationDefault.toErrorResponse(HttpStatusCode.BadRequest)
                }
            }
        }
    }
}

private fun RouteConfig.existsDisplayIdDocs() {
    summary = "사용자 표시 ID 존재 여부 확인"
    description = "사용자 표시 ID 존재 여부 확인"
    request {
        pathParameter<String>("displayId") {
            description = "사용자 표시 ID"
            example("sample") {
                value = "honggd"
            }
        }
    }

    response {
        code(HttpStatusCode.OK) {
            body<ExistsResultResponse> {
                example("ExistsResultResponse") {
                    value = """
                        {
                            "exists": true
                        }
                    """.trimIndent()
                }
            }
        }

        code(HttpStatusCode.BadRequest) {
            body<ErrorResponse> {
                example("ErrorResponse") {
                    value = CommonErrorCode.ValidationDefault.toErrorResponse(HttpStatusCode.BadRequest)
                }
            }
        }
    }
}
