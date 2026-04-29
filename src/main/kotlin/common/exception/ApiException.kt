package com.turnin.common.exception

import io.ktor.http.HttpStatusCode

/**
 * 커스텀 예외 타입
 *
 * 커스텀 예외 타입을 구현할 때 이 예외 타입을 상속하여 사용해야 한다.
 *
 * ##### 사용 예시
 * ```
 * class NotFoundException(message: String = "Resource not found") :
 *     ApiException(ApiErrorCode.NotFound, HttpStatusCode.NotFound, message)
 * ```
 *
 * @param errorCode [ApiErrorCode] 에러 코드
 * @param status [HttpStatusCode] HTTP 상태 코드
 * @param message 에러 메시지 (디버깅/로깅 용)
 * @param cause 스택트레이스를 위한 예외 전달 용 (디버깅/로깅 용)
 */
open class ApiException(
    val errorCode: ApiErrorCode,
    val status: HttpStatusCode,
    override val message: String,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause)
