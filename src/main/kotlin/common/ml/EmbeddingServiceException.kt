package com.peekr.common.ml

import com.peekr.common.exception.ApiErrorCode
import com.peekr.common.exception.ApiException
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
    class InferenceException :
        EmbeddingServiceException(
            code = EmbeddingServiceErrorCode.InferenceError,
            status = HttpStatusCode.InternalServerError,
        )

    /** 토큰화 실패 예외 */
    class TokenizationFailed :
        EmbeddingServiceException(
            code = EmbeddingServiceErrorCode.TokenizationFailed,
            status = HttpStatusCode.InternalServerError,
        )

    /** 임베딩 서비스 초기화 실패 예외 */
    class InitializationFailed :
        EmbeddingServiceException(
            code = EmbeddingServiceErrorCode.InitializationFailed,
            status = HttpStatusCode.InternalServerError,
        )
}
