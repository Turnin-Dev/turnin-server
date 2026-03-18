package com.peekr.domain.notification.exception

import com.peekr.common.exception.ApiErrorCode

sealed class NotificationErrorCode(
    raw: String,
    description: String,
) : ApiErrorCode(raw, description) {
    data object InvalidPersonalNotificationType :
        NotificationErrorCode(NOTI001, "개인 알림은 NOTICE, EVENT 타입을 사용할 수 없습니다.")

    data object InvalidBroadcastNotificationType :
        NotificationErrorCode(NOTI002, "브로드캐스트 알림은 NOTICE, EVENT 타입만 사용할 수 있습니다.")
}

private const val NOTI001 = "NOTI001"
private const val NOTI002 = "NOTI002"
