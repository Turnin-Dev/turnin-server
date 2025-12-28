package com.peekr.common.util.pagination.cursor

import com.peekr.common.validator.inputValidationAndReturn
import io.ktor.server.routing.RoutingContext

fun RoutingContext.getCursorPaginationParams(): CursorPaginationParams<Long> {
    val cursor = call.request.queryParameters["cursor"]
        ?.toLongOrNull()
        .inputValidationAndReturn("CursorPaginationParams(cursor)")
    val size = call.request.queryParameters["size"]
        ?.toIntOrNull()
        .inputValidationAndReturn("CursorPaginationParams(pageSize)")

    if (size < 1) throw IllegalArgumentException("Page size number must be positive.")

    return CursorPaginationParams(
        cursor = cursor,
        size = size,
    )
}
