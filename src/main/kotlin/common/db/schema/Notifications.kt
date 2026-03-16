package com.peekr.common.db.schema

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import com.peekr.common.model.NotificationType
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption

/** 알림 엔티티 클래스 (Exposed DSL 방식) */
object Notifications : BaseLongIdTable("notification") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE).nullable()
    val notiType = enumerationByName<NotificationType>("noti_type", 50)
    val title = varchar("title", 200).nullable()
    val message = text("message")
    val imageUrl = varchar("image_url", 500).nullable()
    val isRead = bool("is_read").default(false)
    val isBroadcast = bool("is_broadcast").default(false)
    val refId = long("ref_id").nullable()
    val refType = varchar("ref_type", 50).nullable()

    init {
        index("idx_notification_user_id", false, userId)
        index("idx_notification_broadcast", false, isBroadcast)
        // 나머지 unread 인덱스(idx_notification_unread)는 SQL 파일에만 유지
    }
}

/** 알림 엔티티 클래스 (Exposed DAO/ORM 방식) */
class NotificationEntity(id: EntityID<Long>) : BaseEntity(id, Notifications) {
    companion object : BaseEntityClass<NotificationEntity>(Notifications)

    var userId by Notifications.userId
    var notiType by Notifications.notiType
    var title by Notifications.title
    var message by Notifications.message
    var imageUrl by Notifications.imageUrl
    var isRead by Notifications.isRead
    var isBroadcast by Notifications.isBroadcast
    var refId by Notifications.refId
    var refType by Notifications.refType
}
