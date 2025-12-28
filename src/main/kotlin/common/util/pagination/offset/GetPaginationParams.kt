package com.peekr.common.util.pagination.offset

import com.peekr.common.validator.inputValidationAndReturn
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.routing.RoutingContext

/**
 * 페이지네이션(오프셋 기반) 파라미터를 가져온다.
 */
fun RoutingContext.getPaginationParams(): PaginationParams {
    val page = call.request.queryParameters["page"]
        ?.toLongOrNull()
        .inputValidationAndReturn("PaginationParams(page)")
    val size = call.request.queryParameters["size"]
        ?.toIntOrNull()
        .inputValidationAndReturn("PaginationParams(pageSize)")

    when {
        page < 1 -> BadRequestException("Page number must be positive.")
        size < 1 -> BadRequestException("Page size number must be positive.")
        size > 25 -> BadRequestException("Page size number too large.")
    }

    return PaginationParams(
        page = page,
        size = size,
    )
}
