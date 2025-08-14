package com.peekr.common.db.schema

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption

object Notifications : BaseLongIdTable("notification") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.RESTRICT)
    val notiType = varchar("noti_type", 50)
    val message = text("message")
    val isRead = bool("is_read").default(false)

    init {
        // 일반적으로 “읽지 않은 알림 조회”가 많으므로 (user_id, is_read) 인덱스 추가
        index("idx_notification_user_is_read", false, userId, isRead, createdAt)
    }
}

class NotificationEntity(id: EntityID<Long>) : BaseEntity(id, Notifications) {
    companion object : BaseEntityClass<NotificationEntity>(Notifications)

    var userId by Notifications.userId
    var notiType by Notifications.notiType
    var message by Notifications.message
    var isRead by Notifications.isRead
}
