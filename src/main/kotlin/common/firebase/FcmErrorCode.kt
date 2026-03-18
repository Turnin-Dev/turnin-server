package com.peekr.common.firebase

import com.peekr.common.exception.ApiErrorCode

sealed class FcmErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    data object TopicOrTokenMustBeSet :
        FcmErrorCode(FCM001, "token 또는 topic 중 하나는 반드시 있어야 합니다.")

    data object TokenMustBeSet :
        FcmErrorCode(FCM002, "단일 전송 시 token은 필수입니다.")

    data object TopicMustBeSet :
        FcmErrorCode(FCM003, "토픽 전송 시 topic은 필수입니다.")
}

private const val FCM001 = "FCM001"
private const val FCM002 = "FCM002"
private const val FCM003 = "FCM003"
