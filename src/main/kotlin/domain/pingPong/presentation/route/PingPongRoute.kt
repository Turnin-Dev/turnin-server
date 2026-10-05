package com.turnin.domain.pingPong.presentation.route

import com.turnin.common.exception.common.CommonErrorCode
import com.turnin.common.plugin.AuthenticatedRoute
import com.turnin.common.plugin.RateLimitToken
import com.turnin.common.route.Api
import com.turnin.common.util.pagination.cursor.CursorPage
import com.turnin.common.util.pagination.cursor.getCursorPaginationParams
import com.turnin.common.util.pagination.cursor.toResponse
import com.turnin.common.validator.inputValidationAndReturn
import com.turnin.domain.pingPong.application.usecase.PingPongUseCases
import com.turnin.domain.pingPong.domain.model.PingPongContent
import com.turnin.domain.pingPong.exception.PingPongErrorCode
import com.turnin.domain.pingPong.presentation.dto.CreatePingPongAnswerRequest
import com.turnin.domain.pingPong.presentation.dto.CreatePingPongQuestionRequest
import com.turnin.domain.pingPong.presentation.dto.PingPongAnswerResponse
import com.turnin.domain.pingPong.presentation.dto.PingPongDetailResponse
import com.turnin.domain.pingPong.presentation.dto.PingPongResponse
import com.turnin.domain.pingPong.presentation.dto.toResponse
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
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
        rateLimit(RateLimitToken.CREATE_PING_PONG_QUESTION_BURST.ktorName) {
            rateLimit(RateLimitToken.CREATE_PING_PONG_QUESTION.ktorName) {
                post(route.byUserKeyword(pathParam = "{userKeywordId}"), { createQuestionDocs() }) {
                    val userKeywordIdParam = call.parameters["userKeywordId"]
                        ?.toLongOrNull()
                        .inputValidationAndReturn("사용자 키워드 ID")
                    val questionerId = extractUserIdWithToken()
                    val createQuestionRequest = call.receive<CreatePingPongQuestionRequest>()
                    val pingPongDto = usecase.createQuestion(
                        questionerId,
                        userKeywordIdParam,
                        createQuestionRequest.question,
                    )
                    call.respond(HttpStatusCode.Created, pingPongDto.toResponse())
                }
            }
        }

        post(route.answer(pathParam = "{pingPongId}"), { createAnswerDocs() }) {
            val pingPongIdParam = call.parameters["pingPongId"]
                ?.toLongOrNull()
                .inputValidationAndReturn("핑퐁 ID")
            val answererId = extractUserIdWithToken()
            val createAnswerRequest = call.receive<CreatePingPongAnswerRequest>()
            val answerDto = usecase.createAnswer(
                answererId,
                pingPongIdParam,
                createAnswerRequest.answer,
            )
            call.respond(HttpStatusCode.Created, answerDto.toResponse())
        }

        get(route.byUserKeyword(pathParam = "{userKeywordId}"), { getPingPongsDocs() }) {
            val userKeywordIdParam = call.parameters["userKeywordId"]
                ?.toLongOrNull()
                .inputValidationAndReturn("사용자 키워드 ID")
            val currentUserId = extractUserIdWithToken()
            val params = getCursorPaginationParams()
            val cursorPage = usecase.getPingPongs(
                currentUserId,
                userKeywordIdParam,
                params.cursor,
                params.size,
            )
            val response = cursorPage.toResponse { pingPongDetailDto ->
                pingPongDetailDto.toResponse()
            }
            call.respond(HttpStatusCode.OK, response)
        }

        delete(route.byId(pathParam = "{pingPongId}"), { deleteQuestionDocs() }) {
            val pingPongIdParam = call.parameters["pingPongId"]
                ?.toLongOrNull()
                .inputValidationAndReturn("핑퐁 ID")
            val requesterId = extractUserIdWithToken()
            usecase.deleteQuestion(requesterId, pingPongIdParam)
            call.respond(HttpStatusCode.NoContent)
        }

        delete(route.answer(pathParam = "{pingPongId}"), { deleteAnswerDocs() }) {
            val pingPongIdParam = call.parameters["pingPongId"]
                ?.toLongOrNull()
                .inputValidationAndReturn("핑퐁 ID")
            val requesterId = extractUserIdWithToken()
            usecase.deleteAnswer(requesterId, pingPongIdParam)
            call.respond(HttpStatusCode.NoContent)
        }
    }
}

private fun RouteConfig.createQuestionDocs() {
    summary = "질문 작성"
    description = """
        게시물(사용자 키워드)에 질문을 등록한다.

        - 질문은 공백만으로 이루어질 수 없으며, 최대 ${PingPongContent.MAX_LENGTH}자까지 작성할 수 있다.
        - Rate Limit (동일 사용자 + 동일 게시물 기준): ${RateLimitToken.CREATE_PING_PONG_QUESTION_BURST.toPrettyString()}, ${RateLimitToken.CREATE_PING_PONG_QUESTION.toPrettyString()}
        - 등록 시 게시물 작성자에게 `PING_PONG_QUESTION` 알림을 전송한다. (딥링크 데이터는 알림 목록 조회 API 참고)
    """.trimIndent()
    request {
        pathParameter<Long>("userKeywordId") {
            description = "질문을 등록할 사용자 키워드(게시물) ID"
        }
        body<CreatePingPongQuestionRequest> {
            description = "질문 작성 요청 바디"
            example("CreatePingPongQuestionRequest") {
                value = CreatePingPongQuestionRequest.sample
            }
        }
    }
    response {
        code(HttpStatusCode.Created) {
            body<PingPongResponse> {
                description = "생성된 핑퐁"
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

private fun RouteConfig.createAnswerDocs() {
    summary = "답변 작성"
    description = """
        질문에 답변을 등록한다.

        - 질문이 달린 게시물(사용자 키워드)의 작성자만 답변을 등록할 수 있으며, 질문당 답변은 1개만 등록할 수 있다.
        - 질문자와 차단 관계(양방향)이면 답변을 등록할 수 없다. (차단 전에 달린 질문은 삭제/신고만 가능)
        - 답변은 공백만으로 이루어질 수 없으며, 최대 ${PingPongContent.MAX_LENGTH}자까지 작성할 수 있다.
        - 등록 시 질문자에게 `PING_PONG_ANSWER` 알림을 전송한다. (딥링크 데이터는 알림 목록 조회 API 참고)
    """.trimIndent()
    request {
        pathParameter<Long>("pingPongId") {
            description = "답변을 등록할 질문의 핑퐁 ID"
        }
        body<CreatePingPongAnswerRequest> {
            description = "답변 작성 요청 바디"
            example("CreatePingPongAnswerRequest") {
                value = CreatePingPongAnswerRequest.sample
            }
        }
    }
    response {
        code(HttpStatusCode.Created) {
            body<PingPongAnswerResponse> {
                description = "생성된 답변"
                example("PingPongAnswerResponse") {
                    value = PingPongAnswerResponse.sample
                }
            }
        }
        code(HttpStatusCode.BadRequest) {
            description = """
                답변이 비어있거나 최대 글자 수를 초과한 경우 (`${CommonErrorCode.ValidationDefault.code}`),
                요청 바디 또는 핑퐁 ID 형식이 잘못된 경우 (`${CommonErrorCode.MalformedRequest.code}`)

                - UI 메시지: "요청을 처리할 수 없어요. 잠시 후 다시 시도해 주세요."
            """.trimIndent()
        }
        code(HttpStatusCode.Forbidden) {
            description = """
                게시물 작성자가 아닌 사용자가 답변을 등록하려는 경우 (`${PingPongErrorCode.NotUserKeywordOwner.code}`)

                - UI 메시지: "게시물 작성자만 답변할 수 있어요."

                질문자와 차단 관계인 경우 (`${PingPongErrorCode.CannotAnswerBlockedQuestioner.code}`)

                - UI 메시지: "답변할 수 없는 질문이에요."
            """.trimIndent()
        }
        code(HttpStatusCode.NotFound) {
            description = """
                핑퐁이 없거나 질문이 숨김 처리된 경우 (`${PingPongErrorCode.PingPongNotFound.code}`)

                - UI 메시지: "삭제되었거나 볼 수 없는 질문이에요."

                질문이 달린 게시물을 조회할 수 없는 경우 (`${PingPongErrorCode.UserKeywordNotFound.code}`)

                - UI 메시지: "삭제되었거나 볼 수 없는 게시물이에요."
            """.trimIndent()
        }
        code(HttpStatusCode.Conflict) {
            description = """
                이미 답변이 등록된 질문인 경우 (`${PingPongErrorCode.AlreadyAnswered.code}`)

                - UI 메시지: "이미 답변한 질문이에요."
            """.trimIndent()
        }
    }
}

private fun RouteConfig.getPingPongsDocs() {
    summary = "핑퐁 목록 조회"
    description = """
        게시물(사용자 키워드)에 달린 핑퐁(질문 + 답변) 목록을 최신순으로 조회한다. (커서 기반 페이지네이션)

        - 신고 누적으로 숨김 처리된 질문, 비활성화된 질문자, 조회자와 차단 관계인 질문자의 핑퐁은 목록에서 제외된다.
        - 단, 게시물 작성자가 조회하면 차단 관계인 질문자의 핑퐁(차단 전에 달린 질문)도 포함된다. (작성자가 삭제/신고할 수 있도록)
        - 답변이 없거나 신고 누적으로 숨김 처리된 경우 `answer`는 `null`이다.
    """.trimIndent()
    request {
        pathParameter<Long>("userKeywordId") {
            description = "조회할 사용자 키워드(게시물) ID"
        }
        queryParameter<Long?>("cursor") {
            description = "페이지네이션에 필요한 커서 값 (초기 호출 시 null 로 요청, 이후 응답의 nextCursor 사용)"
        }
        queryParameter<Int>("size") {
            description = "페이지네이션에 필요한 페이지 크기 (1 ~ 25)"
        }
    }
    response {
        code(HttpStatusCode.OK) {
            body<CursorPage<PingPongDetailResponse, Long>> {
                description = "핑퐁 목록 (다음 페이지가 없으면 nextCursor는 null)"
                example("CursorPage(PingPongDetailResponse)") {
                    value = PingPongDetailResponse.sample
                }
            }
        }
        code(HttpStatusCode.BadRequest) {
            description = """
                게시물 ID가 0 이하이거나, 커서가 0 이하인 경우 (`${CommonErrorCode.ValidationDefault.code}`),
                게시물 ID 또는 페이지 크기 형식이 잘못되었거나, 페이지 크기가 1 ~ 25 범위를 벗어난 경우 (`${CommonErrorCode.MalformedRequest.code}`)

                - UI 메시지: "요청을 처리할 수 없어요. 잠시 후 다시 시도해 주세요."
            """.trimIndent()
        }
        code(HttpStatusCode.NotFound) {
            description = """
                게시물을 조회할 수 없는 경우 (존재하지 않음, 비활성화, 차단 관계) (`${PingPongErrorCode.UserKeywordNotFound.code}`)

                - UI 메시지: "삭제되었거나 볼 수 없는 게시물이에요."
            """.trimIndent()
        }
    }
}

private fun RouteConfig.deleteQuestionDocs() {
    summary = "질문 삭제"
    description = """
        질문을 삭제한다. 질문에 달린 답변도 함께 삭제된다.

        - 질문자 본인 또는 질문이 달린 게시물(사용자 키워드)의 작성자만 삭제할 수 있다.
        - 차단 관계여도 차단 전에 달린 질문은 각 사용자가 직접 삭제할 수 있다.
    """.trimIndent()
    request {
        pathParameter<Long>("pingPongId") {
            description = "삭제할 질문의 핑퐁 ID"
        }
    }
    response {
        code(HttpStatusCode.NoContent) {
            description = "삭제 성공"
        }
        code(HttpStatusCode.BadRequest) {
            description = """
                핑퐁 ID가 0 이하인 경우 (`${CommonErrorCode.ValidationDefault.code}`),
                핑퐁 ID 형식이 잘못된 경우 (`${CommonErrorCode.MalformedRequest.code}`)

                - UI 메시지: "요청을 처리할 수 없어요. 잠시 후 다시 시도해 주세요."
            """.trimIndent()
        }
        code(HttpStatusCode.Forbidden) {
            description = """
                질문자도 게시물 작성자도 아닌 사용자가 삭제하려는 경우 (`${PingPongErrorCode.NoPermissionToDelete.code}`)

                - UI 메시지: "삭제할 수 없는 질문이에요."
            """.trimIndent()
        }
        code(HttpStatusCode.NotFound) {
            description = """
                핑퐁이 없거나 질문이 숨김 처리된 경우 (`${PingPongErrorCode.PingPongNotFound.code}`)

                - UI 메시지: "이미 삭제되었거나 볼 수 없는 질문이에요."
            """.trimIndent()
        }
    }
}

private fun RouteConfig.deleteAnswerDocs() {
    summary = "답변 삭제"
    description = """
        질문에 달린 답변을 삭제한다. 질문은 유지되며, 삭제 후 다시 답변할 수 있다.

        - 질문이 달린 게시물(사용자 키워드)의 작성자만 삭제할 수 있다.
    """.trimIndent()
    request {
        pathParameter<Long>("pingPongId") {
            description = "삭제할 답변이 달린 핑퐁 ID"
        }
    }
    response {
        code(HttpStatusCode.NoContent) {
            description = "삭제 성공"
        }
        code(HttpStatusCode.BadRequest) {
            description = """
                핑퐁 ID가 0 이하인 경우 (`${CommonErrorCode.ValidationDefault.code}`),
                핑퐁 ID 형식이 잘못된 경우 (`${CommonErrorCode.MalformedRequest.code}`)

                - UI 메시지: "요청을 처리할 수 없어요. 잠시 후 다시 시도해 주세요."
            """.trimIndent()
        }
        code(HttpStatusCode.Forbidden) {
            description = """
                게시물 작성자가 아닌 사용자가 삭제하려는 경우 (`${PingPongErrorCode.NoPermissionToDelete.code}`)

                - UI 메시지: "삭제할 수 없는 답변이에요."
            """.trimIndent()
        }
        code(HttpStatusCode.NotFound) {
            description = """
                핑퐁이 없거나 질문이 숨김 처리된 경우 (`${PingPongErrorCode.PingPongNotFound.code}`)

                - UI 메시지: "이미 삭제되었거나 볼 수 없는 질문이에요."

                답변이 없거나 숨김 처리된 경우 (`${PingPongErrorCode.PingPongAnswerNotFound.code}`)

                - UI 메시지: "이미 삭제되었거나 볼 수 없는 답변이에요."
            """.trimIndent()
        }
    }
}
