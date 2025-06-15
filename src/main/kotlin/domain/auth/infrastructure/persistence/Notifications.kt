package com.peekr.domain.auth.infrastructure.persistence

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID

object Notifications : BaseLongIdTable("notification") {
    val userId = reference("user_id", Users)
    val notiType = varchar("noti_type", 50)
    val message = text("message")
    val isRead = bool("is_read").default(false)
}

class Notification(id: EntityID<Long>) : BaseEntity(id, Notifications) {
    companion object : BaseEntityClass<Notification>(Notifications)

    var userId by Notifications.userId
    var notiType by Notifications.notiType
    var message by Notifications.message
    var isRead by Notifications.isRead
}
