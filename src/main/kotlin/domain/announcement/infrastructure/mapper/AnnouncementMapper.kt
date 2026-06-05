package com.turnin.domain.announcement.infrastructure.mapper

import com.turnin.common.db.schema.Announcements
import com.turnin.common.model.id.AnnouncementId
import com.turnin.domain.announcement.domain.model.Announcement
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.ResultRow

object AnnouncementMapper {
    fun ResultRow.toDomain(
        isRead: Expression<Boolean>,
    ): Announcement =
        Announcement(
            id = AnnouncementId(this[Announcements.id].value),
            title = this[Announcements.title],
            content = this[Announcements.content],
            targetAudience = this[Announcements.targetAudience],
            expiresAt = this[Announcements.expiresAt]?.toEpochSecond(),
            createdAt = this[Announcements.createdAt].toEpochSecond(),
            isRead = this[isRead],
        )
}
