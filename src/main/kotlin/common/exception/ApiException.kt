package com.peekr.common.exception

import io.ktor.http.HttpStatusCode

/**
 * 커스텀 예외 타입
 *
 * 커스텀 예외 타입을 구현할 때 이 예외 타입을 상속하여 사용해야 한다.
 *
 * ##### 사용 예시
 * ```
 * class NotFoundException(message: String = "Resource not found") :
 *     ApiException("NOT_FOUND", message, HttpStatusCode.NotFound)
 * ```
 *
 * @param errorCode [ApiErrorCode] 에러 코드
 * @param status [HttpStatusCode] HTTP 상태 코드
 * @param message 에러 메시지 (디버깅/로그 용, 필요 시 민감한 정보를 제외하고 클라이언트에게 노출)
 * @param throwable 스택트레이스를 위한 예외 전달 용 (필요시 메시지를 꺼낼 수 있다)
 */
open class ApiException(
    val errorCode: ApiErrorCode,
    val status: HttpStatusCode,
    override val message: String,
    throwable: Throwable? = null,
) : RuntimeException(message, throwable)
