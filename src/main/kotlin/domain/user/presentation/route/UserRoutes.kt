package com.peekr.domain.user.presentation.route

import com.peekr.common.exception.ErrorResponse
import com.peekr.common.exception.toErrorResponse
import com.peekr.common.plugin.AuthenticatedRoute
import com.peekr.common.route.Api
import com.peekr.common.route.Api.byPathParam
import com.peekr.common.validator.inputValidationAndReturn
import com.peekr.domain.user.application.usecase.UserUseCases
import com.peekr.domain.user.exception.UserErrorCode
import com.peekr.domain.user.presentation.dto.IntroducePatchRequest
import com.peekr.domain.user.presentation.dto.MyProfileResponse
import com.peekr.domain.user.presentation.dto.UserPatchRequest
import com.peekr.domain.user.presentation.dto.UserProfileResponse
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
fun AuthenticatedRoute.userRoutes(route: Api.V1.User, usecase: UserUseCases) {
    route(route.ROUTE, {
        tags = setOf(route.TAG)
        description = "User API"
    }) {
        get({ getUserByIdDocs() }) {
            val userId = extractUserIdWithToken()
            val user = usecase.get(userId)
            if (user != null) {
                call.respond(user.toResponse())
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    UserErrorCode.UserNotFound.toErrorResponse(HttpStatusCode.NotFound),
                )
            }
        }

        get(route.MY_PROFILE, { getMyProfileDocs() }) {
            val userId = extractUserIdWithToken()
            val user = usecase.getMyProfile(userId)
            if (user != null) {
                call.respond(user.toResponse())
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    UserErrorCode.UserNotFound.toErrorResponse(HttpStatusCode.NotFound),
                )
            }
        }

        get(route.PROFILE.byPathParam("displayId"), { getUserProfileDocs() }) {
            val myUserId = extractUserIdWithToken()
            val displayId = call.pathParameters["displayId"].inputValidationAndReturn("사용자 표시 ID")
            val userProfileDto = usecase.getUserProfile(myUserId = myUserId, displayId = displayId)
            if (userProfileDto != null) {
                call.respond(userProfileDto.toResponse())
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    UserErrorCode.UserNotFound.toErrorResponse(HttpStatusCode.NotFound),
                )
            }
        }

        patch({ patchUserDocs() }) {
            val userPatchRequest = call.receive<UserPatchRequest>()
            val userId = extractUserIdWithToken()
            verifyAuthUserId(userId)
            val result = usecase.update(userId, userPatchRequest.toDto())
            if (result) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    UserErrorCode.UserPatchFailed.toErrorResponse(HttpStatusCode.NotFound),
                )
            }
        }

        patch(route.INTRODUCE, { patchIntroduceDocs() }) {
            val introducePatchRequest = call.receive<IntroducePatchRequest>()
            val userId = extractUserIdWithToken()
            verifyAuthUserId(userId)
            val result = usecase.updateIntroduce(userId, introducePatchRequest.introduce)
            if (result) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(
                    HttpStatusCode.NotFound,
                    UserErrorCode.IntroducePatchFailed.toErrorResponse(HttpStatusCode.NotFound),
                )
            }
        }
    }
}

// ------------------------------ Route Docs ------------------------------
private fun RouteConfig.getUserByIdDocs() {
    summary = "사용자 조회"
    description = "사용자 ID로 사용자를 조회한다."
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

private fun RouteConfig.getMyProfileDocs() {
    summary = "나의 프로필 조회"
    description = "나의 사용자 ID로 프로필을 조회한다."
    response {
        code(HttpStatusCode.OK) {
            body<MyProfileResponse> {
                description = "나의 프로필"
            }
        }
        code(HttpStatusCode.NotFound) {
            body<ErrorResponse> {
                description = "사용자가 존재하지 않는 경우"
                example("UserNotFound") {
                    value = UserErrorCode.UserNotFound.toErrorResponse(HttpStatusCode.NotFound)
                }
            }
        }
    }
}

private fun RouteConfig.getUserProfileDocs() {
    summary = "사용자 프로필 조회"
    description = "사용자 표시 ID로 사용자 프로필을 조회한다."
    request {
        pathParameter<String>("displayId") {
            description = "사용자 표시 ID"
            example("Example") {
                value = "display-id"
            }
        }
    }
    response {
        code(HttpStatusCode.OK) {
            body<UserProfileResponse> {
                description = "사용자 프로필"
            }
        }
        code(HttpStatusCode.NotFound) {
            body<ErrorResponse> {
                description = "사용자가 존재하지 않는 경우"
                example("UserNotFound") {
                    value = UserErrorCode.UserNotFound.toErrorResponse(HttpStatusCode.NotFound)
                }
            }
        }
    }
}

private fun RouteConfig.patchUserDocs() {
    summary = "사용자 정보 수정"
    description = "사용자 정보를 수정한다."
    request {
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
                description = "사용자 정보가 수정되지 않았을 때"
                example("UserPatchFailed") {
                    value = UserErrorCode.UserPatchFailed.toErrorResponse(HttpStatusCode.NotFound)
                }
            }
        }
    }
}

private fun RouteConfig.patchIntroduceDocs() {
    summary = "사용자 소개글 수정"
    description = "사용자 소개글을 수정한다."
    request {
        body<IntroducePatchRequest> {
            description = "사용자 소개글 수정 요청 바디"
            example("Example") {
                value = IntroducePatchRequest.sample
            }
        }
    }
    response {
        code(HttpStatusCode.NoContent) {
            description = "사용자 소개글 수정 성공 시"
        }
        code(HttpStatusCode.NotFound) {
            body<ErrorResponse> {
                description = "소개글이 수정되지 않았을 때"
                example("IntroducePatchFailed") {
                    value = UserErrorCode.IntroducePatchFailed.toErrorResponse(HttpStatusCode.NotFound)
                }
            }
        }
    }
}
