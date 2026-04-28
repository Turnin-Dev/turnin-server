package com.turnin.domain.file.exception

import com.turnin.common.exception.ApiErrorCode
import com.turnin.common.exception.ApiException
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

    /** S3Presigner 서명 생성 과정에서 에러 발생 */
    class S3CredentialException(cause: Throwable) :
        FileException(
            code = FileErrorCode.S3CredentialFailed,
            status = HttpStatusCode.InternalServerError,
            cause = cause,
        )

    /** R2 파일 삭제 과정에서 에러 발생 */
    class R2DeleteFailed(cause: Throwable) :
        FileException(
            code = FileErrorCode.R2DeleteFailed,
            status = HttpStatusCode.InternalServerError,
            cause = cause,
        )
}
