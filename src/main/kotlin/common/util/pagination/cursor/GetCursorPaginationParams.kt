package com.turnin.common.util.pagination.cursor

import com.turnin.common.validator.inputValidationAndReturn
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.routing.RoutingContext

/**
 * Long 타입의 커서를 사용하는 페이지네이션 파라미터 입력 및 검증
 */
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

/**
 * String 타입의 커서를 사용하는 페이지네이션 파라미터 입력 및 검증
 *
 * @param sizeLimit 페이지 사이즈 제한
 *
 * @throws BadRequestException 페이지 사이즈 제한 위반 시 예외 발생
 */
fun RoutingContext.getStringCursorPaginationParams(
    sizeLimit: Int = 30,
): CursorPaginationParams<String> {
    val cursor = call.request.queryParameters["cursor"]
        ?.takeIf { it.isNotBlank() }

    val size = call.request.queryParameters["size"]
        ?.toIntOrNull()
        .inputValidationAndReturn("CursorPaginationParams(pageSize)")

    when {
        size < 1 -> throw BadRequestException("Page size number must be positive.")
        size > sizeLimit -> throw BadRequestException("Page size number too large.")
    }

    return CursorPaginationParams(
        cursor = cursor,
        size = size,
    )
}
