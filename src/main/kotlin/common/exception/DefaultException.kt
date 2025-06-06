package com.peekr.common.exception

/**
 * 범용적으로 사용하는 기본 예외 타입
 *
 * 커스텀 예외 타입을 구현할 때 이 예외 타입을 상속하여 사용해야 한다.
 *
 * @sample com.peekr.common.jwt.exception.TokenException
 */
open class DefaultException(
    val code: DefaultErrorCode,
    override val message: String,
) : RuntimeException(message)
