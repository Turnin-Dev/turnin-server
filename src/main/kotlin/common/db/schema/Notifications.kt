package com.peekr.common.db.schema

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption

/** 알림 엔티티 클래스 (복수형) */
object Notifications : BaseLongIdTable("notification") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.RESTRICT)
    val notiType = varchar("noti_type", 50)
    val message = text("message")
    val isRead = bool("is_read").default(false)
    // TODO 확장 필요
    // -- notification 테이블에 관련 엔티티 ID 추가 (확장성)
    // ALTER TABLE notification
    // ADD COLUMN related_id BIGINT, -- 관련 엔티티의 ID (friend_id, keyword_id 등)
    // ADD COLUMN related_type VARCHAR(50); -- 관련 엔티티 타입

    init {
        // 일반적으로 “읽지 않은 알림 조회”가 많으므로 (user_id, is_read) 인덱스 추가
        index("idx_notification_user_is_read", false, userId, isRead, createdAt)
    }
}

/** 알림 엔티티 클래스 (단수형) */
class NotificationEntity(id: EntityID<Long>) : BaseEntity(id, Notifications) {
    companion object : BaseEntityClass<NotificationEntity>(Notifications)

    var userId by Notifications.userId
    var notiType by Notifications.notiType
    var message by Notifications.message
    var isRead by Notifications.isRead
}
