package com.turnin.common.firebase

import com.turnin.common.exception.ApiErrorCode

sealed class FcmErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    data object TokenMustBeSet :
        FcmErrorCode(FCM001, "단일 전송 시 token은 필수입니다.")

    data object TopicMustBeSet :
        FcmErrorCode(FCM002, "토픽 전송 시 topic은 필수입니다.")
}

private const val FCM001 = "FCM001"
private const val FCM002 = "FCM002"
