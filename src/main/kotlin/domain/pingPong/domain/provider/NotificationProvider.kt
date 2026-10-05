package com.turnin.domain.pingPong.domain.provider

import com.turnin.domain.pingPong.domain.model.PingPongNotificationCommand

/**
 * 외부에서 제공받은 알림 API
 */
interface NotificationProvider {
    /**
     * 알림을 전송한다. (알림 내역 저장 + FCM 전송)
     *
     * @param command 알림 커맨드
     */
    suspend fun sendNotification(command: PingPongNotificationCommand)
}
