package com.peekr.common.validator

import com.peekr.common.exception.ApiException
import com.peekr.common.exception.CommonErrorCode
import io.ktor.http.HttpStatusCode

/** 유효성 검사 유틸 */
object PeekrValidator {
    /**
     * 유효성 검사에서 사용하고, [require] 대신 사용
     *
     * [value]로 유효성 검사를 체크하고 실패 시, [lazyMessage]를 던진다.
     *
     * @param value 유효성 검사 조건 ([require]의 value)
     * @param lazyMessage 유효성 검사 조건 실패 시 ([require]의 lazyMessage)
     * @throws ValidatorException 일반적으로 유효성 검사 실패 시 해당 예외를 반환한다.
     * @throws ApiException 예상치 못한 예외 발생 시 해당 예외를 반환한다.
     */
    fun validation(
        value: Boolean,
        lazyMessage: () -> Any,
    ) {
        try {
            require(
                value = value,
                lazyMessage = lazyMessage,
            )
        } catch (e: IllegalArgumentException) {
            throw ValidatorException(e.message ?: DEFAULT_ERROR_MESSAGE)
        } catch (e: Exception) {
            throw ApiException(
                errorCode = CommonErrorCode.Unexpected,
                message = e.message ?: CommonErrorCode.Unexpected.description,
                status = HttpStatusCode.InternalServerError,
            )
        }
    }
}

private const val DEFAULT_ERROR_MESSAGE = "유효성 검사 실패"
