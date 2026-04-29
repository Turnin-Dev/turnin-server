package com.turnin.domain.friend.exception

import com.turnin.common.exception.ApiErrorCode

sealed class FriendErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    data object SelfRequestError :
        FriendErrorCode(F001, "본인에게 요청 할 수 없습니다.")

    data object UserNotFound :
        FriendErrorCode(F002, "사용자를 찾을 수 없습니다.")

    data object AlreadyFriendRequest :
        FriendErrorCode(F003, "이미 친구 요청을 한 상태입니다.")
}

private const val F001 = "F001"
private const val F002 = "F002"
private const val F003 = "F003"
