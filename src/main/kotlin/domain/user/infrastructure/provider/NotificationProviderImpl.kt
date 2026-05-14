package com.turnin.domain.user.infrastructure.provider

import com.turnin.common.model.id.UserId
import com.turnin.domain.notification.application.provider.NotificationProviderApi
import com.turnin.domain.user.domain.provider.NotificationProvider

class NotificationProviderImpl(private val notificationProviderApi: NotificationProviderApi) : NotificationProvider {
    override suspend fun deactivate(userId: UserId, token: String) =
        notificationProviderApi.deactivate(userId, token)
}
