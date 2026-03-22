package com.peekr.domain.friend.domain.model

import com.peekr.common.model.NotificationType
import com.peekr.common.model.id.UserId

data class FriendNotificationCommand(
    val userId: UserId,
    val notiType: NotificationType,
    val title: String,
    val message: String,
    val refId: Long? = null,
    val refType: String? = null,
)
