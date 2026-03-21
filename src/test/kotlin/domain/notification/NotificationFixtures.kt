package com.peekr.domain.notification

import com.peekr.common.model.NotificationType
import com.peekr.common.model.id.FcmTokenId
import com.peekr.common.model.id.NotificationId
import com.peekr.common.model.id.UserId
import com.peekr.domain.notification.domain.model.FcmToken
import com.peekr.domain.notification.domain.model.Notification
import com.peekr.domain.notification.domain.model.NotificationCommand

/** Notification 생성 유틸 */
fun notificationFixture(
    id: Long = 1L,
    userId: UserId? = UserId(1L),
    command: NotificationCommand? = null,
) = Notification(
    id = NotificationId(id),
    userId = userId,
    notiType = command?.notiType ?: NotificationType.FRIEND_REQUEST,
    title = command?.title ?: "테스트 알림",
    message = command?.message ?: "테스트 메시지",
    imageUrl = command?.imageUrl,
    isRead = false,
    isBroadcast = command?.isBroadcast ?: false,
    refId = command?.refId,
    refType = command?.refType,
    createdAt = System.currentTimeMillis() / 1000,
    updatedAt = System.currentTimeMillis() / 1000,
)

/** FcmToken 생성 유틸 */
fun fcmTokenFixture(
    id: Long = 1L,
    userId: UserId = UserId(1L),
    token: String = "test_fcm_token",
) = FcmToken(
    id = FcmTokenId(id),
    userId = userId,
    token = token,
    isActive = true,
    createdAt = System.currentTimeMillis() / 1000,
    updatedAt = System.currentTimeMillis() / 1000,
)
