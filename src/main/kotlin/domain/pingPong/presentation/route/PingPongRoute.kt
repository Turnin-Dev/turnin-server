package com.turnin.domain.pingPong.presentation.route

import com.turnin.common.plugin.AuthenticatedRoute
import com.turnin.common.plugin.RateLimitToken
import com.turnin.common.route.Api
import com.turnin.common.validator.inputValidationAndReturn
import com.turnin.domain.pingPong.application.usecase.PingPongUseCases
import com.turnin.domain.pingPong.domain.model.PingPongContent
import com.turnin.domain.pingPong.presentation.dto.CreatePingPongRequest
import com.turnin.domain.pingPong.presentation.dto.PingPongResponse
import com.turnin.domain.pingPong.presentation.dto.toResponse
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.request.receive
import io.ktor.server.response.respond

fun AuthenticatedRoute.pingPongRoutes(route: Api.V1.PingPong, usecase: PingPongUseCases) {
    route({
        tags = setOf(route.TAG)
        description = "PingPong API"
    }) {
        // 동일 사용자가 동일 게시물에 5초당 1회 / 1분당 5회 (두 토큰을 중첩 적용)
        rateLimit(RateLimitToken.CREATE_PING_PONG_BURST.ktorName) {
            rateLimit(RateLimitToken.CREATE_PING_PONG.ktorName) {
                post(route.byUserKeyword(pathParam = "{userKeywordId}"), { createPingPongDocs() }) {
                    val userKeywordIdParam = call.parameters["userKeywordId"]
                        ?.toLongOrNull()
                        .inputValidationAndReturn("사용자 키워드 ID")
                    val questionerId = extractUserIdWithToken()
                    val createPingPongRequest = call.receive<CreatePingPongRequest>()
                    val pingPongDto = usecase.create(
                        questionerId,
                        userKeywordIdParam,
                        createPingPongRequest.question,
                    )
                    call.respond(HttpStatusCode.Created, pingPongDto.toResponse())
                }
            }
        }
    }
}

private fun RouteConfig.createPingPongDocs() {
    summary = "핑퐁(질문) 작성"
    description = """
        게시물(사용자 키워드)에 질문을 등록한다.

        - 질문은 공백만으로 이루어질 수 없으며, 최대 ${PingPongContent.MAX_LENGTH}자까지 작성할 수 있다.
        - Rate Limit (동일 사용자 + 동일 게시물 기준): ${RateLimitToken.CREATE_PING_PONG_BURST.toPrettyString()}, ${RateLimitToken.CREATE_PING_PONG.toPrettyString()}
    """.trimIndent()
    request {
        pathParameter<Long>("userKeywordId") {
            description = "질문을 등록할 사용자 키워드(게시물) ID"
        }
        body<CreatePingPongRequest> {
            description = "핑퐁(질문) 작성 요청 바디"
            example("CreatePingPongRequest") {
                value = CreatePingPongRequest.sample
            }
        }
    }
    response {
        code(HttpStatusCode.Created) {
            body<PingPongResponse> {
                description = "생성된 핑퐁(질문)"
                example("PingPongResponse") {
                    value = PingPongResponse.sample
                }
            }
        }
        code(HttpStatusCode.BadRequest) {
            description = "질문이 비어있거나 최대 글자 수를 초과한 경우, 본인 게시물에 질문하는 경우, 요청 형식이 잘못된 경우"
        }
        code(HttpStatusCode.NotFound) {
            description = "게시물을 조회할 수 없는 경우 (존재하지 않음, 비활성화, 차단 관계)"
        }
        code(HttpStatusCode.TooManyRequests) {
            description = "작성 간격 제한을 초과한 경우"
        }
    }
}
