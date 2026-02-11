package com.peekr.domain.block.presentation.route

import com.peekr.common.model.id.UserId
import com.peekr.common.plugin.AuthenticatedRoute
import com.peekr.common.route.Api
import com.peekr.common.util.pagination.offset.getPaginationParams
import com.peekr.common.validator.inputValidationAndReturn
import com.peekr.domain.block.application.usecase.BlockUseCases
import com.peekr.domain.block.presentation.dto.BlockDetailRequest
import com.peekr.domain.block.presentation.dto.BlockReasonResponse
import com.peekr.domain.block.presentation.dto.BlocksResponse
import com.peekr.domain.block.presentation.dto.toDto
import com.peekr.domain.block.presentation.dto.toResponse
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
        get({ getBlocksDocs() }) {
            val userId = extractUserIdWithToken()
            val paginationParams = getPaginationParams()
            val blocksPagingDataDto = usecase.getBlocks(userId.value, paginationParams)
            call.respond(HttpStatusCode.OK, blocksPagingDataDto.toResponse())
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
            usecase.deleteBlock(blockId)
            call.respond(HttpStatusCode.OK)
        }
    }
}

private fun RouteConfig.getBlocksDocs() {
    summary = "차단 목록 조회 (페이지네이션)"
    description = "차단 목록을 조회한다. (페이지네이션)"
    request {
        queryParameter<Long>("page") {
            description = "페이지네이션에 필요한 페이지 번호"
        }
        queryParameter<Int>("size") {
            description = "페이지네이션에 필요한 페이지 크기"
        }
    }

    response {
        code(HttpStatusCode.OK) {
            description = "차단 목록"
            body<BlocksResponse> {
                example("BlocksResponse") {
                    value = BlocksResponse.sample
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
            description = "차단 성공 시"
        }
    }
}
