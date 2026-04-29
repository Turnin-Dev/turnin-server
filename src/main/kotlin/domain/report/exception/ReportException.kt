package com.turnin.domain.report.exception

import com.turnin.common.exception.ApiErrorCode
import com.turnin.common.exception.ApiException
import io.ktor.http.HttpStatusCode

sealed class ReportException(
    code: ApiErrorCode,
    status: HttpStatusCode,
    message: String = code.description,
    cause: Throwable? = null,
) : ApiException(code, status, message, cause) {
    /** 필수 신고 대상이 빠진 경우 */
    class MissingReportTargetException(cause: Throwable? = null) :
        ReportException(
            code = ReportErrorCode.MissingReportTarget,
            status = HttpStatusCode.BadRequest,
            cause = cause,
        )

    class CannotReportMySelf(cause: Throwable? = null) :
        ReportException(
            code = ReportErrorCode.CannotReportMySelf,
            status = HttpStatusCode.BadRequest,
            cause = cause,
        )
}
