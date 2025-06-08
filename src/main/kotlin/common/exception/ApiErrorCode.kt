package com.peekr.common.exception

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
 * @param description 에러 코드 설명 (디버깅, 로그 용)
 */
open class ApiErrorCode(
    val code: String,
    val description: String,
)

/** [ApiErrorCode.code]에 대한 코드 생성기 */
object RawErrorCodeFactory {
    /**
     * ##### 사용 예시
     * ```
     * val HEADER = "A"
     * HEADER.toErrorCode(1)
     * // -> A001 출력
     * ```
     * @param this 에러 코드 헤더 문자열
     * @param num 에러 코드 번호
     */
    fun String.toErrorCode(num: Int): String = this + "$num".padStart(3, '0')
}
