package com.peekr.domain.userKeyword.infrastructure.provider

import com.peekr.common.model.NotificationType
import com.peekr.domain.notification.application.provider.NotificationProviderApi
import com.peekr.domain.userKeyword.domain.provider.NotificationProvider

class NotificationProviderImpl(private val notificationProviderApi: NotificationProviderApi) : NotificationProvider {
    override suspend fun sendNotificationToTokens(
        tokens: List<String>,
        notiType: NotificationType,
        title: String,
        message: String,
        refId: Long?,
        refType: String?,
        senderUserId: Long?,
    ) {
        notificationProviderApi.sendNotificationToTokens(
            tokens = tokens,
            notiType = notiType,
            title = title,
            message = message,
            refId = refId,
            refType = refType,
            senderUserId = senderUserId,
        )
    }
}
