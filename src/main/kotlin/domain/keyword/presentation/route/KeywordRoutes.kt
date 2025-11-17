package com.peekr.domain.keyword.presentation.route

import com.peekr.common.model.KeywordId
import com.peekr.common.plugin.AuthenticatedRoute
import com.peekr.common.route.Api
import com.peekr.common.route.Api.byPathParam
import com.peekr.common.validator.inputValidationAndReturn
import com.peekr.domain.keyword.application.usecase.KeywordUseCases
import com.peekr.domain.keyword.presentation.dto.CreateKeywordRequest
import com.peekr.domain.keyword.presentation.dto.KeywordResponse
import com.peekr.domain.keyword.presentation.dto.toResponse
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond

fun AuthenticatedRoute.keywordRoutes(route: Api.V1.Keyword, usecase: KeywordUseCases) {
    route(route.ROUTE, {
        tags = setOf(route.TAG)
        description = "Keyword API"
    }) {
        get(route.ID.byPathParam("keywordId"), { getKeywordByIdDocs() }) {
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

        get(route.NAME.byPathParam("keywordName"), { getKeywordByNameDocs() }) {
            val keywordName = call.pathParameters["keywordName"].inputValidationAndReturn("키워드 명")
            val keywordDto = usecase.getByName(keywordName)
            if (keywordDto == null) {
                call.respond(HttpStatusCode.NotFound)
            } else {
                call.respond(HttpStatusCode.OK, keywordDto.toResponse())
            }
        }

        post({ createKeywordDocs() }) {
            val createKeywordRequest = call.receive<CreateKeywordRequest>()
            val createById = extractUserIdWithToken()
            val keywordResult = usecase.create(
                keywordName = createKeywordRequest.keyword,
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
            body<KeywordResponse> {
                description = "키워드"
                example("KeywordResponse") {
                    value = KeywordResponse.sample
                }
            }
        }
        code(HttpStatusCode.NotFound) {
            description = "키워드가 존재하지 않는 경우"
        }
    }
}

private fun RouteConfig.getKeywordByNameDocs() {
    summary = "키워드 조회"
    description = "키워드 명으로 키워드를 조회한다."
    request {
        pathParameter<String>("keywordName") {
            description = "키워드 명"
            example("Example") {
                value = "Sample KeywordName"
            }
        }
    }
    response {
        code(HttpStatusCode.OK) {
            body<KeywordResponse> {
                description = "키워드"
                example("KeywordResponse") {
                    value = KeywordResponse.sample
                }
            }
        }
        code(HttpStatusCode.NotFound) {
            description = "키워드가 존재하지 않는 경우"
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
            body<KeywordResponse> {
                description = "키워드 생성 응답 바디"
                example("KeywordResponse") {
                    value = KeywordResponse.sample
                }
            }
        }
    }
}
