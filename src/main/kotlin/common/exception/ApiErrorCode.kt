package com.peekr.common.exception

import io.ktor.http.HttpStatusCode

/**
 * 커스텀 에러 코드
 *
 * 커스텀 에러 코드를 구현할 때 이 에러 코드 클래스를 상속하여 사용해야 한다.
 *
 * ##### 사용 예시
 * ```
 * sealed class TokenErrorCode(
 *     value: String = TOKEN_ERROR_VALUE,
 *     description: String,
 * ) : ApiErrorCode(value, description) {
 *     data object InvalidToken : TokenErrorCode(description = "Invalid token")
 * }
 * ```
 *
 * @param code 에러 코드 문자열 값
 * @param description 에러 코드 설명 (클라이언트 노출 용)
 */
open class ApiErrorCode(
    val code: String,
    val description: String,
)

fun ApiErrorCode.toErrorResponse(status: HttpStatusCode): ErrorResponse = ErrorResponse(
    code = this.code,
    message = description,
    status = status.value,
)
