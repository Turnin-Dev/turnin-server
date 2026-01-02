package com.peekr.domain.userKeyword.presentation.route

import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.common.plugin.AuthenticatedRoute
import com.peekr.common.route.Api
import com.peekr.common.validator.inputValidationAndReturn
import com.peekr.domain.userKeyword.application.usecase.UserKeywordUseCases
import com.peekr.domain.userKeyword.presentation.dto.CreateUserKeywordRequest
import com.peekr.domain.userKeyword.presentation.dto.DescriptionResponse
import com.peekr.domain.userKeyword.presentation.dto.GetUserKeywordResponse
import com.peekr.domain.userKeyword.presentation.dto.UpdateDescriptionRequest
import com.peekr.domain.userKeyword.presentation.dto.UserKeywordResponse
import com.peekr.domain.userKeyword.presentation.dto.toDto
import com.peekr.domain.userKeyword.presentation.dto.toResponse
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.patch
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond

fun AuthenticatedRoute.userKeywordRoutes(route: Api.V1.UserKeyword, usecase: UserKeywordUseCases) {
    route(route.ROUTE, {
        tags = setOf(route.TAG)
        description = "User Keyword API"
    }) {
        get({ getUserKeywordsDocs() }) {
            val userId = call.queryParameters["userId"]
                ?.toLongOrNull()
                .inputValidationAndReturn("사용자 ID")
            val userKeywords = usecase.get(userId)
            call.respond(userKeywords.toResponse())
        }

        get(route.GET_DESCRIPTION, { getDescriptionDocs() }) {
            val userKeywordIdParam = call.queryParameters["userKeywordId"]
                ?.toLongOrNull()
                .inputValidationAndReturn("사용자 키워드 ID")
            val userKeywordId = UserKeywordId(userKeywordIdParam)
            val ownerId = extractUserIdWithToken()
            verifyAuthUserId(ownerId)
            val descriptionDto = usecase.getDescription(ownerId, userKeywordId)
            if (descriptionDto != null) {
                call.respond(HttpStatusCode.OK, descriptionDto.toResponse())
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        post({ createUserKeywordDocs() }) {
            val createUserKeywordRequest = call.receive<CreateUserKeywordRequest>()
            val ownerId = UserId(createUserKeywordRequest.userId)
            verifyAuthUserId(ownerId)
            val addUserKeywordRequestDto = createUserKeywordRequest.toDto().copy(userId = ownerId)
            val userKeywordDto = usecase.create(addUserKeywordRequestDto)
            call.respond(HttpStatusCode.Created, userKeywordDto.toResponse())
        }

        patch(route.PATCH_DESCRIPTION, { updateDescriptionDocs() }) {
            val userKeywordIdParam = call.queryParameters["userKeywordId"]
                ?.toLongOrNull()
                .inputValidationAndReturn("사용자 키워드 ID")
            val ownerId = extractUserIdWithToken()
            verifyAuthUserId(ownerId)
            val userKeywordId = UserKeywordId(userKeywordIdParam)
            val descriptionDto = call.receive<UpdateDescriptionRequest>().toDto()
            val result = usecase.updateDescription(
                ownerId = ownerId,
                userKeywordId = userKeywordId,
                patch = descriptionDto,
            )
            if (result != null) {
                call.respond(HttpStatusCode.OK, result.toResponse())
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        delete({ deleteUserKeywordDocs() }) {
            val userKeywordIdParam = call.queryParameters["userKeywordId"]
                ?.toLongOrNull()
                .inputValidationAndReturn("사용자 키워드 ID")
            val ownerId = extractUserIdWithToken()
            verifyAuthUserId(ownerId)
            val userKeywordId = UserKeywordId(userKeywordIdParam)
            val result = usecase.delete(ownerId, userKeywordId)
            if (result) {
                call.respond(HttpStatusCode.NoContent)
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }
    }
}

private fun RouteConfig.getUserKeywordsDocs() {
    summary = "사용자 키워드 목록 조회"
    description = "사용자 ID로 사용자 키워드 목록을 조회한다."
    request {
        queryParameter<Long>("userId") {
            description = "사용자 ID"
            example("userId") {
                value = 1
            }
        }
    }
    response {
        code(HttpStatusCode.OK) {
            body<GetUserKeywordResponse> {
                description = "사용자 키워드 목록"
                example("NonEmpty") { value = GetUserKeywordResponse.sample }
                example("Empty") { value = GetUserKeywordResponse.sample.copy(emptyList()) }
            }
        }
    }
}

private fun RouteConfig.getDescriptionDocs() {
    summary = "사용자 키워드 설명 조회"
    description = "사용자 키워드 ID를 통해 사용자 키워드 설명을 조회한다."
    request {
        queryParameter<Long>("userKeywordId") {
            description = "사용자 키워드 ID"
            example("userKeywordId") {
                value = 1
            }
        }
    }
    response {
        code(HttpStatusCode.OK) {
            body<DescriptionResponse> {
                description = "사용자 키워드 설명 응답 바디 (성공)"
                example("DescriptionResponse") {
                    value = DescriptionResponse.sample
                }
            }
        }
        code(HttpStatusCode.NotFound) {
            description = "사용자 키워드 설명 응답 바디 (실패)"
        }
    }
}

private fun RouteConfig.createUserKeywordDocs() {
    summary = "사용자 키워드 생성"
    description = "사용자 키워드를 생성한다."
    request {
        body<CreateUserKeywordRequest> {
            description = "사용자 키워드 생성 요청 바디"
            example("CreateUserKeywordRequest") {
                value = CreateUserKeywordRequest.sample
            }
        }
    }
    response {
        code(HttpStatusCode.Created) {
            body<UserKeywordResponse> {
                description = "사용자 키워드 생성 응답 바디"
                example("UserKeywordResponse") {
                    value = UserKeywordResponse.sample
                }
            }
        }
    }
}

private fun RouteConfig.updateDescriptionDocs() {
    summary = "사용자 키워드 설명 수정"
    description = "사용자 키워드 설명을 수정한다."
    request {
        queryParameter<Long>("userKeywordId") {
            description = "사용자 키워드 ID"
            example("userKeywordId") {
                value = 1
            }
        }
        body<UpdateDescriptionRequest> {
            description = "사용자 키워드 설명 수정 요청 바디"
            example("UpdateDescriptionRequest") {
                value = UpdateDescriptionRequest.sample
            }
        }
    }
    response {
        code(HttpStatusCode.OK) {
            body<DescriptionResponse> {
                description = "사용자 키워드 설명 수정 응답 결과 (성공)"
                example("UpdateDescriptionResponse") {
                    value = DescriptionResponse.sample
                }
            }
        }
        code(HttpStatusCode.NotFound) {
            description = "사용자 키워드 설명 수정 응답 결과 (실패)"
        }
    }
}

private fun RouteConfig.deleteUserKeywordDocs() {
    summary = "사용자 키워드 삭제"
    description = "사용자 키워드를 삭제한다."
    request {
        queryParameter<Long>("userKeywordId") {
            description = "사용자 키워드 ID"
            example("userKeywordId") {
                value = 1
            }
        }
    }
    response {
        code(HttpStatusCode.NoContent) {
            description = "사용자 키워드 삭제 응답 결과 (성공)"
        }
        code(HttpStatusCode.NotFound) {
            description = "사용자 키워드 삭제 응답 결과 (실패)"
        }
    }
}
