package com.turnin.domain.block.presentation.route

import com.turnin.common.model.id.UserId
import com.turnin.common.plugin.AuthenticatedRoute
import com.turnin.common.route.Api
import com.turnin.common.util.pagination.cursor.CursorPage
import com.turnin.common.util.pagination.cursor.getCursorPaginationParams
import com.turnin.common.util.pagination.cursor.toResponse
import com.turnin.common.validator.inputValidationAndReturn
import com.turnin.domain.block.application.usecase.BlockUseCases
import com.turnin.domain.block.presentation.dto.BlockDetailRequest
import com.turnin.domain.block.presentation.dto.BlockReasonResponse
import com.turnin.domain.block.presentation.dto.BlockedUserResponse
import com.turnin.domain.block.presentation.dto.toDto
import com.turnin.domain.block.presentation.dto.toResponse
import io.github.smiley4.ktoropenapi.config.RouteConfig
import io.github.smiley4.ktoropenapi.delete
import io.github.smiley4.ktoropenapi.get
import io.github.smiley4.ktoropenapi.post
import io.github.smiley4.ktoropenapi.route
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond

fun AuthenticatedRoute.blockRoutes(route: Api.V1.Block, usecase: BlockUseCases) {
    route(route.ROUTE, {
        tags = setOf(route.TAG)
        description = "Block API"
    }) {
        get({ getBlockedUsersDocs() }) {
            val userId = extractUserIdWithToken()
            val cursorPaginationParams = getCursorPaginationParams()
            val cursorPage = usecase.getBlockedUsers(
                userId = userId.value,
                cursor = cursorPaginationParams.cursor,
                pageSize = cursorPaginationParams.size,
            )
            val response = cursorPage.toResponse { blockedUserDto ->
                blockedUserDto.toResponse()
            }
            call.respond(HttpStatusCode.OK, response)
        }

        get(route.REASON, { getBlockReasonsDocs() }) {
            val reasons = usecase.getBlockReasons()
            call.respond(HttpStatusCode.OK, reasons.toResponse())
        }

        post({ createBlockDocs() }) {
            val blockDetailRequest = call.receive<BlockDetailRequest>()
            val blockerId = UserId(blockDetailRequest.blockerId)
            verifyAuthUserId(blockerId)
            usecase.createBlock(blockDetailRequest.toDto())
            call.respond(HttpStatusCode.Created)
        }

        delete({ deleteBlockDocs() }) {
            val blockId = call.request.queryParameters["blockId"]
                ?.toLongOrNull()
                .inputValidationAndReturn("차단 ID")
            val ownerId = extractUserIdWithToken()
            usecase.deleteBlock(ownerId.value, blockId)
            call.respond(HttpStatusCode.OK)
        }
    }
}

private fun RouteConfig.getBlockedUsersDocs() {
    summary = "차단 사용자 목록 조회 (페이지네이션)"
    description = "차단 사용자 목록을 조회한다. (페이지네이션)"
    request {
        queryParameter<Long?>("cursor") {
            description = "페이지네이션에 필요한 커서 값 (초기 호출 시 null 로 요청)"
        }
        queryParameter<Int>("size") {
            description = "페이지네이션에 필요한 페이지 크기"
        }
    }

    response {
        code(HttpStatusCode.OK) {
            description = "차단 사용자 목록"
            body<CursorPage<BlockedUserResponse, Long>> {
                example("CursorPage(BlockedUserResponse)") {
                    value = BlockedUserResponse.sample
                }
            }
        }
    }
}

private fun RouteConfig.getBlockReasonsDocs() {
    summary = "차단 사유 목록 조회"
    description = "차단 사유 목록을 조회한다."
    response {
        code(HttpStatusCode.OK) {
            body<List<BlockReasonResponse>> {
                description = "차단 사유 목록"
                example("BlockReasonsResponse") {
                    value = BlockReasonResponse.sampleList
                }
            }
        }
    }
}

private fun RouteConfig.createBlockDocs() {
    summary = "차단 생성"
    description = "차단을 생성한다."
    request {
        body<BlockDetailRequest> {
            description = "차단 요청 바디"
            example("BlockDetailRequest") {
                value = BlockDetailRequest.sample
            }
        }
    }

    response {
        code(HttpStatusCode.Created) {
            description = "차단 요청 성공 시"
        }
        code(HttpStatusCode.Forbidden) {
            description = "차단 요청자와 blockerId가 일치하지 않는 경우"
        }
    }
}

private fun RouteConfig.deleteBlockDocs() {
    summary = "차단 해제"
    description = "차단을 해제한다. (삭제)"
    request {
        queryParameter<Long>("blockId") {
            description = "차단 ID"
        }
    }

    response {
        code(HttpStatusCode.OK) {
            description = "차단 해제 성공 시"
        }
    }
}
