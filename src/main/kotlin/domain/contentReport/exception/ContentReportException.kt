package com.turnin.domain.contentReport.exception

import com.turnin.common.exception.ApiErrorCode
import com.turnin.common.exception.ApiException
import io.ktor.http.HttpStatusCode

/**
 * 콘텐츠 신고 커스텀 예외
 */
sealed class ContentReportException(
    code: ApiErrorCode,
    status: HttpStatusCode,
    message: String = code.description,
    cause: Throwable? = null,
) : ApiException(code, status, message, cause) {
    /** 신고할 콘텐츠가 없거나, 이미 숨김 처리된 경우 */
    class ContentNotFound :
        ContentReportException(
            code = ContentReportErrorCode.ContentNotFound,
            status = HttpStatusCode.NotFound,
        )

    /** 본인이 작성한 콘텐츠를 신고하려는 경우 */
    class CannotReportOwnContent :
        ContentReportException(
            code = ContentReportErrorCode.CannotReportOwnContent,
            status = HttpStatusCode.BadRequest,
        )

    /** 이미 신고한 콘텐츠를 다시 신고하려는 경우 */
    class AlreadyReported(cause: Throwable? = null) :
        ContentReportException(
            code = ContentReportErrorCode.AlreadyReported,
            status = HttpStatusCode.Conflict,
            cause = cause,
        )

    /** 존재하지 않는 신고 사유로 신고하려는 경우 */
    class InvalidReportReason(cause: Throwable? = null) :
        ContentReportException(
            code = ContentReportErrorCode.InvalidReportReason,
            status = HttpStatusCode.BadRequest,
            cause = cause,
        )
}
