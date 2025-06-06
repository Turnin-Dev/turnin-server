package com.peekr.common.exception

/**
 * [DefaultException]에서 사용하는 에러 코드 모음
 *
 * @param value 에러 코드 값
 * @param description 에러 코드 설명 (추후에 로그용으로 사용 예정)
 */
enum class DefaultErrorCode(
    val value: String,
    private val description: String,
) {
    Token("token", "token error"),
}
