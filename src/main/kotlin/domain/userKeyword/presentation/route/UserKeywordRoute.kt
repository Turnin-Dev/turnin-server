package com.turnin.domain.userKeyword.presentation.route

import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.common.plugin.AuthenticatedRoute
import com.turnin.common.plugin.RateLimitToken
import com.turnin.common.route.Api
import com.turnin.common.validator.inputValidationAndReturn
import com.turnin.domain.userKeyword.application.usecase.UserKeywordUseCases
import com.turnin.domain.userKeyword.presentation.dto.CreateUserKeywordRequest
import com.turnin.domain.userKeyword.presentation.dto.UpdateUserKeywordRequest
import com.turnin.domain.userKeyword.presentation.dto.UserKeywordDetailResponse
import com.turnin.domain.userKeyword.presentation.dto.UserKeywordResponse
import com.turnin.domain.userKeyword.presentation.dto.UserKeywordsResponse
import com.turnin.domain.userKeyword.presentation.dto.toDto
import com.turnin.domain.userKeyword.presentation.dto.toResponse
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.patch
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond

fun AuthenticatedRoute.userKeywordRoutes(route: Api.V1.UserKeyword, usecase: UserKeywordUseCases) {
    route({
        tags = setOf(route.TAG)
        description = "User Keyword API"
    }) {
        get(route.ROUTE, { getUserKeywordsDocs() }) {
            val userId = call.queryParameters["userId"]
                ?.toLongOrNull()
                .inputValidationAndReturn("사용자 ID")
            val userKeywords = usecase.get(userId)
            call.respond(userKeywords.toResponse())
        }

        get(route.detail(pathParam = "{userKeywordId}"), { getDetailDocs() }) {
            val userKeywordIdParam = call.parameters["userKeywordId"]
                ?.toLongOrNull()
                .inputValidationAndReturn("사용자 키워드 ID")
            val currentUserId = extractUserIdWithToken()
            val userKeywordDto = usecase.getDetail(currentUserId.value, userKeywordIdParam)
            if (userKeywordDto != null) {
                call.respond(userKeywordDto.toResponse())
            } else {
                call.respond(HttpStatusCode.NotFound)
            }
        }

        rateLimit(RateLimitToken.CREATE_KEYWORD.ktorName) {
            post(route.ROUTE, { createUserKeywordDocs() }) {
                val createUserKeywordRequest = call.receive<CreateUserKeywordRequest>()
                val ownerId = UserId(createUserKeywordRequest.userId)
                verifyAuthUserId(ownerId)
                val addUserKeywordRequestDto = createUserKeywordRequest.toDto().copy(userId = ownerId)
                val userKeywordDto = usecase.create(addUserKeywordRequestDto)
                call.respond(HttpStatusCode.Created, userKeywordDto.toResponse())
            }
        }

        patch(route.ROUTE, { updateUserKeywordDocs() }) {
            val updateUserKeywordRequest = call.receive<UpdateUserKeywordRequest>()
            val ownerId = extractUserIdWithToken()
            usecase.update(ownerId.value, updateUserKeywordRequest.toDto())
            call.respond(HttpStatusCode.OK)
        }

        delete(route.ROUTE, { deleteUserKeywordDocs() }) {
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
    deprecated = true
    description = "(사용자 ID로 사용자 키워드 목록을 조회한다.)\n" +
        "이 API는 더 이상 사용되지 않습니다. 대신 /api/v1/user/{userId}/keywords를 사용하세요."
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
            body<UserKeywordsResponse> {
                description = "사용자 키워드 목록"
                example("NonEmpty") { value = UserKeywordsResponse.sample }
                example("Empty") { value = UserKeywordsResponse.sample.copy(emptyList()) }
            }
        }
    }
}

private fun RouteConfig.getDetailDocs() {
    summary = "사용자 키워드 상세 정보 조회"
    description = "사용자 키워드 상세 정보를 조회한다.\n" +
        "상세 정보에는 키워드 정보, 사용자 정보 일부가 포함되어있다."
    request {
        pathParameter<Long>("userKeywordId") {
            description = "사용자 키워드 ID"
        }
    }
    response {
        code(HttpStatusCode.OK) {
            body<UserKeywordDetailResponse> {
                description = "사용자 키워드 상세 정보 응답바디"
                example("UserKeywordDetailResponse") {
                    value = UserKeywordDetailResponse.sample
                }
            }
        }
        code(HttpStatusCode.NotFound) {
            description = "사용자 키워드를 조회할 수 없는 경우, 차단된 사용자의 키워드 조회 시"
        }
    }
}

private fun RouteConfig.createUserKeywordDocs() {
    summary = "사용자 키워드 생성"
    description = """
        사용자 키워드를 생성한다.

        - Rate Limit: ${RateLimitToken.CREATE_KEYWORD.toPrettyString()}
    """.trimIndent()
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

private fun RouteConfig.updateUserKeywordDocs() {
    summary = "사용자 키워드 수정"
    description = "사용자 키워드를 수정한다."
    request {
        body<UpdateUserKeywordRequest> {
            description = "사용자 키워드 수정 요청 바디"
            example("UpdateUserKeywordRequest") {
                value = UpdateUserKeywordRequest.sample
            }
        }
    }
    response {
        code(HttpStatusCode.OK) {
            description = "사용자 키워드 수정 성공 시"
        }
        code(HttpStatusCode.NotFound) {
            description = "사용자 키워드를 찾지 못하는 경우"
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
