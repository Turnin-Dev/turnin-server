package com.turnin.domain.friend.infrastructure.provider

import com.turnin.domain.friend.domain.model.FriendNotificationCommand
import com.turnin.domain.friend.domain.provider.NotificationProvider
import com.turnin.domain.notification.application.provider.NotificationProviderApi

class NotificationProviderImpl(private val notificationProviderApi: NotificationProviderApi) : NotificationProvider {
    override suspend fun sendNotification(command: FriendNotificationCommand) {
        notificationProviderApi.sendNotification(
            userId = command.userId,
            notiType = command.notiType,
            title = command.title,
            message = command.message,
            refId = command.refId,
            refType = command.refType,
        )
    }
}
