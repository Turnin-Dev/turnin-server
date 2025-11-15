package com.peekr.common.validator

/**
 * [PeekrValidator]에서 사용하는 예외 타입
 *
 * @param message 유효성 검사 실패/에러 메시지
 */
open class ValidatorException(message: String?) : IllegalArgumentException(message)
