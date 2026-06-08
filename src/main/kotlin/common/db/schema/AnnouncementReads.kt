package com.turnin.common.db.schema

import com.turnin.common.db.DatabaseUtils.timestamptz
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

/**
 * 공지 읽음 엔티티 클래스
 */
object AnnouncementReads : Table("announcement_read") {
    val announcementId = reference("announcement_id", Announcements, onDelete = ReferenceOption.CASCADE)
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val readAt = timestamptz("read_at")

    override val primaryKey = PrimaryKey(announcementId, userId)

    init {
        index("idx_announcement_read_user", false, userId, announcementId)
    }
}
