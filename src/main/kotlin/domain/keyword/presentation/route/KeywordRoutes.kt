package com.peekr.domain.keyword.presentation.route

import com.peekr.common.api.Api
import com.peekr.common.api.Api.byPathParam
import com.peekr.common.exception.ErrorResponse
import com.peekr.common.plugin.AuthenticatedRoute
import com.peekr.domain.core.model.KeywordId
import com.peekr.domain.core.validator.inputValidationAndReturn
import com.peekr.domain.keyword.application.usecase.KeywordUseCases
import com.peekr.domain.keyword.presentation.dto.CreateKeywordRequest
import com.peekr.domain.keyword.presentation.dto.KeywordResponse
import com.peekr.domain.keyword.presentation.dto.toResponse
import com.peekr.domain.user.presentation.dto.UserResponse
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond

fun AuthenticatedRoute.keywordRoutes(route: Api.V1.Keyword, usecase: KeywordUseCases) {
    route({
        tags = setOf(route.TAG)
        description = "Keyword API"
    }) {
        get(route.ROUTE.byPathParam("keywordId"), { getKeywordByIdDocs() }) {
            val keywordIdParam = call.pathParameters["keywordId"]
                ?.toLongOrNull()
                .inputValidationAndReturn("키워드 ID")
            val keywordId = KeywordId(keywordIdParam)
            val keywordDto = usecase.get(keywordId)
            if (keywordDto == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(HttpStatusCode.OK, keywordDto.toResponse())
            }
        }

        post(route.ROUTE, { createKeywordDocs() }) {
            val createKeywordRequest = call.receive<CreateKeywordRequest>()
            val createById = extractUserIdWithToken()
            val keywordResult = usecase.create(
                keyword = createKeywordRequest.keyword,
                createdBy = createById,
            )
            call.respond(HttpStatusCode.Created, keywordResult.toResponse())
        }
    }
}

private fun RouteConfig.getKeywordByIdDocs() {
    summary = "키워드 조회"
    description = "키워드 ID로 키워드를 조회한다."
    request {
        pathParameter<Long>("keywordId") {
            description = "키워드 ID"
            example("Example") {
                value = 1
            }
        }
    }
    response {
        code(HttpStatusCode.OK) {
            body<UserResponse> {
                description = "키워드"
                example("KeywordResponse") {
                    value = KeywordResponse.sample
                }
            }
        }
        code(HttpStatusCode.NotFound) {
            body<ErrorResponse> {
                description = "키워드가 존재하지 않는 경우"
                example("NotFound") {
                    value = 404
                }
            }
        }
    }
}

private fun RouteConfig.createKeywordDocs() {
    summary = "키워드 생성"
    description = "키워드를 생성한다."
    request {
        body<CreateKeywordRequest> {
            description = "키워드 생성 요청 바디"
            example("CreateKeywordRequest") {
                value = CreateKeywordRequest.sample
            }
        }
    }
    response {
        code(HttpStatusCode.Created) {
            body<UserResponse> {
                description = "키워드 생성 응답 바디"
                example("KeywordResponse") {
                    value = KeywordResponse.sample
                }
            }
        }
    }
}
