package com.turnin.domain.friend.domain.model

import com.turnin.common.model.NotificationType
import com.turnin.common.model.id.UserId

data class FriendNotificationCommand(
    val userId: UserId,
    val notiType: NotificationType,
    val title: String,
    val message: String,
    val refId: Long? = null,
    val refType: String? = null,
)
