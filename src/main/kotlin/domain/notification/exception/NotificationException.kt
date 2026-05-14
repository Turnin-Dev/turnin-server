package com.turnin.domain.notification.exception

import com.turnin.common.exception.ApiErrorCode
import com.turnin.common.exception.ApiException
import io.ktor.http.HttpStatusCode

sealed class NotificationException(
    code: ApiErrorCode,
    status: HttpStatusCode,
    message: String = code.description,
    cause: Throwable? = null,
) : ApiException(code, status, message, cause) {
    class MissingUserIdInPersonalNotification :
        NotificationException(
            code = NotificationErrorCode.MissingUserIdInPersonalNotification,
            status = HttpStatusCode.BadRequest,
        )
}
