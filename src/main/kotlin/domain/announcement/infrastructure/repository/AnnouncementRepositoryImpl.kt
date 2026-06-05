package com.turnin.domain.announcement.infrastructure.repository

import com.turnin.common.db.schema.AnnouncementEntity
import com.turnin.common.db.schema.AnnouncementReads
import com.turnin.common.db.schema.Announcements
import com.turnin.common.db.suspendTransaction
import com.turnin.common.db.updateWithTimestamp
import com.turnin.common.model.AnnouncementAudience
import com.turnin.common.model.AnnouncementStatus
import com.turnin.common.model.id.AnnouncementId
import com.turnin.common.model.id.UserId
import com.turnin.common.util.TurninDateTime
import com.turnin.common.util.toOffsetDateTime
import com.turnin.domain.announcement.domain.model.Announcement
import com.turnin.domain.announcement.domain.model.AnnouncementDetail
import com.turnin.domain.announcement.domain.repository.AnnouncementRepository
import com.turnin.domain.announcement.infrastructure.mapper.AnnouncementMapper.toDomain
import java.time.Instant
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertIgnore
import org.jetbrains.exposed.sql.or

class AnnouncementRepositoryImpl : AnnouncementRepository {
    override suspend fun getAnnouncements(
        userId: UserId,
        userRole: AnnouncementAudience,
    ): List<Announcement> = suspendTransaction {
        val isRead = AnnouncementReads.readAt.isNotNull()

        Announcements
            .join(
                otherTable = AnnouncementReads,
                joinType = JoinType.LEFT,
                onColumn = Announcements.id,
                otherColumn = AnnouncementReads.announcementId,
                additionalConstraint = {
                    AnnouncementReads.userId eq userId.value
                },
            ).select(Announcements.columns + isRead)
            .where {
                (Announcements.status eq AnnouncementStatus.ACTIVE) and
                    (Announcements.targetAudience inList listOf(AnnouncementAudience.ALL, userRole)) and
                    (
                        (Announcements.expiresAt eq null) or
                            (Announcements.expiresAt greater TurninDateTime.now().toOffsetDateTime())
                    )
            }.orderBy(Announcements.createdAt to SortOrder.DESC)
            .map { it.toDomain(isRead) }
    }

    override suspend fun createAnnouncement(
        detail: AnnouncementDetail,
    ): Unit = suspendTransaction {
        AnnouncementEntity.new {
            this.title = detail.title
            this.content = detail.content
            this.targetAudience = detail.targetAudience
            this.status = AnnouncementStatus.INACTIVE
            this.expiresAt = detail.expiresAt?.let {
                Instant.ofEpochSecond(it).toOffsetDateTime()
            }
        }
    }

    override suspend fun markAsRead(
        announcementId: AnnouncementId,
        userId: UserId,
    ): Unit = suspendTransaction {
        AnnouncementReads.insertIgnore {
            it[AnnouncementReads.announcementId] = announcementId.value
            it[AnnouncementReads.userId] = userId.value
            it[readAt] = TurninDateTime.now().toOffsetDateTime()
        }
    }

    override suspend fun updateStatus(
        announcementId: AnnouncementId,
        status: AnnouncementStatus,
    ): Boolean = suspendTransaction {
        Announcements.updateWithTimestamp(
            where = { Announcements.id eq announcementId.value },
        ) {
            it[Announcements.status] = status
        } > 0
    }

    override suspend fun deleteAnnouncement(
        announcementId: AnnouncementId,
    ): Boolean = suspendTransaction {
        Announcements.deleteWhere {
            Announcements.id eq announcementId.value
        } > 0
    }
}
