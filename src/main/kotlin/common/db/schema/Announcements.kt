package com.turnin.common.db.schema

import com.turnin.common.db.BaseEntity
import com.turnin.common.db.BaseEntityClass
import com.turnin.common.db.BaseLongIdTable
import com.turnin.common.db.DatabaseUtils.customPostgresEnum
import com.turnin.common.db.DatabaseUtils.timestamptz
import com.turnin.common.model.AnnouncementAudience
import com.turnin.common.model.AnnouncementStatus
import org.jetbrains.exposed.dao.id.EntityID

/** 공지 엔티티 클래스 (Exposed DSL 방식) */
object Announcements : BaseLongIdTable("announcement") {
    val title = varchar("title", 100)
    val content = text("content")
    val targetAudience = customPostgresEnum<AnnouncementAudience>("target_audience", "announcement_audience")
        .default(AnnouncementAudience.ALL)
    val status = customPostgresEnum<AnnouncementStatus>("status", "announcement_status")
        .default(AnnouncementStatus.INACTIVE)
    val expiresAt = timestamptz("expires_at", setDefault = false).nullable()

    init {
        index("idx_announcement_status_expires", false, status, expiresAt)
    }
}

class AnnouncementEntity(id: EntityID<Long>) : BaseEntity(id, Announcements) {
    companion object : BaseEntityClass<AnnouncementEntity>(Announcements)

    var title by Announcements.title
    var content by Announcements.content
    var targetAudience by Announcements.targetAudience
    var status by Announcements.status
    var expiresAt by Announcements.expiresAt
}
