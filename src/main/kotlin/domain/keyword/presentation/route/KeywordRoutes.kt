package com.peekr.domain.keyword.presentation.route

import com.peekr.common.api.Api
import com.peekr.common.api.Api.byPathParam
import com.peekr.domain.core.model.KeywordId
import com.peekr.domain.core.model.UserId
import com.peekr.domain.core.validator.inputValidationAndReturn
import com.peekr.domain.keyword.application.usecase.KeywordUseCases
import com.peekr.domain.keyword.presentation.dto.CreateKeywordRequest
import com.peekr.domain.keyword.presentation.dto.toResponse
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route

fun Route.keywordRoutes(route: Api.V1.Keyword, usecase: KeywordUseCases) {
    route({
        tags = setOf(route.TAG)
        description = "Keyword API"
    }) {
        get(route.ROUTE.byPathParam("keywordId"), {}) {
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

        post(route.ROUTE, {}) {
            val createKeywordRequest = call.receive<CreateKeywordRequest>()
            val createById = UserId(createKeywordRequest.createdBy)
            val keywordResult = usecase.create(
                keyword = createKeywordRequest.keyword,
                createdBy = createById,
            )
            call.respond(HttpStatusCode.Created, keywordResult.toResponse())
        }
    }
}
