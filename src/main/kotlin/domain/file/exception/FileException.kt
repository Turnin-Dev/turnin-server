package com.peekr.domain.file.exception

import com.peekr.common.exception.ApiErrorCode
import com.peekr.common.exception.ApiException
import io.ktor.http.HttpStatusCode

/**
 * File Exception
 *
 * @property code [ApiErrorCode]
 * @property status HTTP 상태코드
 * @property message 에러 메시지
 * @property cause [Throwable]
 */
sealed class FileException(
    code: ApiErrorCode,
    status: HttpStatusCode,
    message: String = code.description,
    cause: Throwable? = null,
) : ApiException(code, status, message, cause) {
    /** S3Presigner를 생성하는 과정에서 잘못된 인자 값 요청으로 에러 발생 */
    class InvalidS3PresignerArgument(cause: Throwable? = null) :
        FileException(
            code = FileErrorCode.InvalidS3PresignerArgument,
            status = HttpStatusCode.BadRequest,
            cause = cause,
        )
}
