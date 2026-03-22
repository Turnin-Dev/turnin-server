package com.peekr.domain.friend.infrastructure.provider

import com.peekr.domain.friend.domain.model.FriendNotificationCommand
import com.peekr.domain.friend.domain.provider.NotificationProvider
import com.peekr.domain.notification.application.provider.NotificationProviderApi

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
