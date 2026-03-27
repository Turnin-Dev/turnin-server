package com.peekr.domain.user.infrastructure.provider

import com.peekr.common.model.id.UserId
import com.peekr.domain.notification.application.provider.NotificationProviderApi
import com.peekr.domain.user.domain.provider.NotificationProvider

class NotificationProviderImpl(private val notificationProviderApi: NotificationProviderApi) : NotificationProvider {
    override suspend fun deactivate(userId: UserId, token: String) =
        notificationProviderApi.deactivate(userId, token)
}
