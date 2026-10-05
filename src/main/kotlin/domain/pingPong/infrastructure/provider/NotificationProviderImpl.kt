package com.turnin.domain.pingPong.infrastructure.provider

import com.turnin.domain.notification.application.provider.NotificationProviderApi
import com.turnin.domain.pingPong.domain.model.PingPongNotificationCommand
import com.turnin.domain.pingPong.domain.provider.NotificationProvider

class NotificationProviderImpl(private val notificationProviderApi: NotificationProviderApi) : NotificationProvider {
    override suspend fun sendNotification(command: PingPongNotificationCommand) {
        notificationProviderApi.sendNotification(
            receiverId = command.receiverId,
            notiType = command.notiType,
            title = command.title,
            message = command.message,
            refId = command.refId,
            refType = command.refType,
            refData = command.refData,
        )
    }
}
