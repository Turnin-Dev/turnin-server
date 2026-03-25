package com.peekr.domain.friend.domain.provider

import com.peekr.domain.friend.domain.model.FriendNotificationCommand

/**
 * 외부에서 제공받은 알림 API
 */
interface NotificationProvider {
    /**
     * 알림을 전송한다.
     *
     * @param command 알림 커맨드
     */
    suspend fun sendNotification(command: FriendNotificationCommand)
}
