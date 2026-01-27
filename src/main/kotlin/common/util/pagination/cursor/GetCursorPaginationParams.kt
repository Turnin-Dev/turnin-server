package com.peekr.common.util.pagination.cursor

import com.peekr.common.validator.inputValidationAndReturn
import com.peekr.domain.feed.application.dto.FeedCursor
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

fun RoutingContext.getFeedCursorPaginationParams(): CursorPaginationParams<FeedCursor> {
    val cursorScore = call.request.queryParameters["cursorScore"]
        ?.takeIf { it.isNotBlank() }
        ?.toDoubleOrNull()
    val cursorCreatedAt = call.request.queryParameters["cursorCreatedAt"]
        ?.takeIf { it.isNotBlank() }
        ?.toLongOrNull()
    val cursorUserKeywordId = call.request.queryParameters["cursorUserKeywordId"]
        ?.takeIf { it.isNotBlank() }
        ?.toLongOrNull()
    val size = call.request.queryParameters["size"]
        ?.toIntOrNull()
        .inputValidationAndReturn("CursorPaginationParams(pageSize)")

    when {
        size < 1 -> throw BadRequestException("Page size number must be positive.")
        size > 25 -> throw BadRequestException("Page size number too large.")
    }

    val feedCursor = FeedCursor(
        score = cursorScore,
        createdAt = cursorCreatedAt,
        userKeywordId = cursorUserKeywordId,
    )

    return CursorPaginationParams(
        cursor = feedCursor,
        size = size,
    )
}
