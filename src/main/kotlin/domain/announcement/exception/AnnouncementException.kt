package com.turnin.domain.announcement.exception

import com.turnin.common.exception.ApiErrorCode
import com.turnin.common.exception.ApiException
import com.turnin.common.model.id.AnnouncementId
import io.ktor.http.HttpStatusCode

sealed class AnnouncementException(
    code: ApiErrorCode,
    status: HttpStatusCode,
    message: String = code.description,
    cause: Throwable? = null,
) : ApiException(code, status, message, cause) {
    /** 공지가 존재하지 않는 경우 발생하는 예외 */
    class NotFound(
        announcementId: AnnouncementId,
        cause: Throwable? = null,
    ) : AnnouncementException(
            code = AnnouncementErrorCode.NotFound,
            status = HttpStatusCode.NotFound,
            message = "${AnnouncementErrorCode.NotFound.description}, announcementId: $announcementId",
            cause = cause,
        )
}
