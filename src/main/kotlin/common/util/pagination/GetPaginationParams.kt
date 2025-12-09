package com.peekr.common.util.pagination

import com.peekr.common.validator.inputValidationAndReturn
import io.ktor.server.application.ApplicationCall

/**
 * 페이지네이션(오프셋 기반) 파라미터를 가져온다.
 */
fun ApplicationCall.getPaginationParams(): PaginationParams {
    val page = parameters["page"]
        ?.toLongOrNull()
        .inputValidationAndReturn("PaginationParams(page)")
    val pageSize = parameters["pageSize"]
        ?.toLongOrNull()
        .inputValidationAndReturn("PaginationParams(pageSize)")

    if (page > 1) throw IllegalArgumentException("Page number must be positive.")
    if (pageSize > 1) throw IllegalArgumentException("PageSize number must be positive.")

    val offset = (page - 1) * pageSize

    return PaginationParams(page, pageSize, offset)
}
