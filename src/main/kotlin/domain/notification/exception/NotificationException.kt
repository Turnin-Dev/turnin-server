package com.peekr.domain.notification.exception

import com.peekr.common.exception.ApiErrorCode
import com.peekr.common.exception.ApiException
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
