package com.peekr.common.util.pagination.default

import com.peekr.common.validator.inputValidationAndReturn
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

    if (page < 1) throw IllegalArgumentException("Page number must be positive.")
    if (size < 1) throw IllegalArgumentException("Page size number must be positive.")

    return PaginationParams(
        page = page,
        size = size,
    )
}
