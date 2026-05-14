package com.turnin.common.ml

import com.turnin.common.exception.ApiErrorCode
import com.turnin.common.exception.ApiException
import io.ktor.http.HttpStatusCode

/**
 * 임베딩 서비스 커스텀 예외
 */
sealed class EmbeddingServiceException(
    code: ApiErrorCode,
    status: HttpStatusCode,
    message: String = code.description,
    cause: Throwable? = null,
) : ApiException(code, status, message, cause) {
    /** 추론 실패 예외 */
    class InferenceException(cause: Throwable? = null) :
        EmbeddingServiceException(
            code = EmbeddingServiceErrorCode.InferenceError,
            status = HttpStatusCode.InternalServerError,
            cause = cause,
        )

    /** 토큰화 실패 예외 */
    class TokenizationFailed(cause: Throwable? = null) :
        EmbeddingServiceException(
            code = EmbeddingServiceErrorCode.TokenizationFailed,
            status = HttpStatusCode.InternalServerError,
            cause = cause,
        )

    /** 임베딩 서비스 초기화 실패 예외 */
    class InitializationFailed(cause: Throwable? = null) :
        EmbeddingServiceException(
            code = EmbeddingServiceErrorCode.InitializationFailed,
            status = HttpStatusCode.InternalServerError,
            cause = cause,
        )
}
