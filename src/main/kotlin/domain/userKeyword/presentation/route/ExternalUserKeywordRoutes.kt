package com.peekr.domain.userKeyword.presentation.route

import com.peekr.common.plugin.AuthenticatedRoute
import com.peekr.common.route.Api
import com.peekr.common.validator.inputValidationAndReturn
import com.peekr.domain.userKeyword.application.usecase.UserKeywordUseCases
import com.peekr.domain.userKeyword.presentation.dto.UserKeywordDetailResponse
import com.peekr.domain.userKeyword.presentation.dto.toResponse
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond

/**
 * [userKeywordRoutes]와 다른점은 같은 도메인의 기능을 사용하지만
 * API 명세서 상에서 외부 도메인에 표시된다.
 *
 * 현재 도메인(UserKeyword) 기준 예시
 * - `userKeywordRoutes`: /api/v1/user-keyword/123
 * - `externalUserKeywordRoutes`: /api/v1/user/123/keywords
 */
fun AuthenticatedRoute.externalUserKeywordRoutes(
    route: Api.V1.User,
    usecase: UserKeywordUseCases,
) {
    route({
        tags = setOf(route.TAG)
    }) {
        get("${route.ROUTE}/{userId}/keywords", { getDetailsDocs() }) {
            val userId = call.parameters["userId"]
                ?.toLongOrNull()
                .inputValidationAndReturn("사용자 ID")
            val userKeywordDetailsDto = usecase.getDetails(userId)
            call.respond(userKeywordDetailsDto.map { it.toResponse() })
        }

        get("${route.ROUTE}/me/keywords", { getMyDetailsDocs() }) {
            val userId = extractUserIdWithToken()
            val userKeywordDetailsDto = usecase.getDetails(userId.value)
            call.respond(userKeywordDetailsDto.map { it.toResponse() })
        }
    }
}

private fun RouteConfig.getDetailsDocs() {
    summary = "사용자 키워드 상세 정보 리스트 조회"
    description = "사용자 ID로 사용자의 키워드 상세 정보 리스트를 조회한다."
    request {
        pathParameter<Long>("userId") {
            description = "사용자 ID"
        }
    }
    response {
        code(HttpStatusCode.OK) {
            body<List<UserKeywordDetailResponse>> {
                example("UserKeywordDetailsResponse") {
                    value = List(2) {
                        UserKeywordDetailResponse.sample
                    }
                }
            }
        }
    }
}

private fun RouteConfig.getMyDetailsDocs() {
    summary = "나의 사용자 키워드 상세 정보 리스트 조회"
    description = "나의 사용자 키워드 상세 정보 리스트를 조회한다."
    response {
        code(HttpStatusCode.OK) {
            body<List<UserKeywordDetailResponse>> {
                example("UserKeywordDetailsResponse") {
                    value = List(2) {
                        UserKeywordDetailResponse.sample
                    }
                }
            }
        }
    }
}
