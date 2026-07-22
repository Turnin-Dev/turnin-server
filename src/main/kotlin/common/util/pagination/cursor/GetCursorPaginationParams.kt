package com.turnin.common.util.pagination.cursor

import com.turnin.common.validator.inputValidationAndReturn
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.routing.RoutingContext

fun RoutingContext.getCursorPaginationParams(): CursorPaginationParams<Long> {
    val cursor = call.request.queryParameters["cursor"]
        ?.takeIf { it.isNotBlank() }
        ?.toLongOrNull()
    val size = call.request.queryParameters["size"]
        ?.toIntOrNull()
        .inputValidationAndReturn("CursorPaginationParams(pageSize)")

    when {
        size < 1 -> throw BadRequestException("Page size number must be positive.")
        size > 25 -> throw BadRequestException("Page size number too large.")
    }

    return CursorPaginationParams(
        cursor = cursor,
        size = size,
    )
}

fun RoutingContext.getFeedCursorPaginationParams(): CursorPaginationParams<String> {
    val cursor = call.request.queryParameters["cursor"]
        ?.takeIf { it.isNotBlank() }

    val size = call.request.queryParameters["size"]
        ?.toIntOrNull()
        .inputValidationAndReturn("CursorPaginationParams(pageSize)")

    when {
        size < 1 -> throw BadRequestException("Page size number must be positive.")
        size > 25 -> throw BadRequestException("Page size number too large.")
    }

    return CursorPaginationParams(
        cursor = cursor,
        size = size,
    )
}
