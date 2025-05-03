package com.peekr.exception

import io.ktor.http.HttpStatusCode

/**
 * 공통적으로 사용하는 예외 처리
 *
 * ##### 사용 예시
 * ```
 * class NotFoundException(message: String = "Resource not found") :
 *     ApiException("NOT_FOUND", message, HttpStatusCode.NotFound)
 * ```
 */
open class ApiException(
    val code: String,
    override val message: String,
    val status: HttpStatusCode
) : RuntimeException(message)