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
 * @param message 에러 메시지 (디버깅, 로그 용)
 * @param status [HttpStatusCode] HTTP 상태 코드
 * @param additional 부가 에러 메시지 (클라이언트 용), 정말로 필요할 때만 사용
 */
open class ApiException(
    val errorCode: ApiErrorCode,
    override val message: String,
    val status: HttpStatusCode,
    val additional: String? = null,
) : RuntimeException(message)
