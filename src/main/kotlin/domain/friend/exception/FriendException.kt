package com.peekr.domain.friend.exception

import com.peekr.common.exception.ApiErrorCode
import com.peekr.common.exception.ApiException
import io.ktor.http.HttpStatusCode

sealed class FriendException(
    code: ApiErrorCode,
    status: HttpStatusCode,
    message: String = code.description,
    cause: Throwable? = null,
) : ApiException(code, status, message, cause) {
    class SelfRequestException :
        FriendException(
            code = FriendErrorCode.SelfRequestError,
            status = HttpStatusCode.BadRequest,
        )

    class UserNotFoundException :
        FriendException(
            code = FriendErrorCode.UserNotFound,
            status = HttpStatusCode.NotFound,
        )

    class AlreadyFriendRequestException(cause: Throwable? = null) :
        FriendException(
            code = FriendErrorCode.AlreadyFriendRequest,
            status = HttpStatusCode.Conflict,
            cause = cause,
        )
}
