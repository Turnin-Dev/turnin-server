package com.peekr.common.exception

import io.ktor.http.HttpStatusCode

/**
 * API 관련 부분에서 사용하는 기본 예외 타입
 *
 * API 커스텀 예외 타입을 구현할 때 이 예외 타입을 상속하여 사용해야 한다.
 *
 * ##### 사용 예시
 * ```
 * class NotFoundException(message: String = "Resource not found") :
 *     ApiException("NOT_FOUND", message, HttpStatusCode.NotFound)
 * ```
 *
 * @param code [ApiErrorCode] API 에러 코드
 * @param message 에러 메시지 ([ApiErrorCode.message] 사용 권장]
 * @param status [HttpStatusCode] HTTP 상태 코드
 */
open class ApiException(
    val code: ApiErrorCode,
    override val message: String,
    val status: HttpStatusCode,
) : RuntimeException(message)
