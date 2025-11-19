package com.peekr.common.exception

/**
 * 베이스 도메인 예외
 *
 * @param message 예외 메시지
 * @param e 예외
 */
open class DomainException(
    message: String,
    e: Throwable? = null,
) : IllegalStateException(message, e)
