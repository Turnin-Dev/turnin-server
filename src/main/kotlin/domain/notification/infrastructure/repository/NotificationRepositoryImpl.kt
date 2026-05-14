package com.turnin.domain.notification.infrastructure.repository

import com.turnin.common.db.schema.NotificationEntity
import com.turnin.common.db.schema.Notifications
import com.turnin.common.db.schema.Users
import com.turnin.common.db.suspendTransaction
import com.turnin.common.db.updateWithTimestamp
import com.turnin.common.model.id.NotificationId
import com.turnin.common.model.id.UserId
import com.turnin.domain.notification.domain.model.Notification
import com.turnin.domain.notification.domain.model.NotificationCommand
import com.turnin.domain.notification.domain.repository.NotificationRepository
import com.turnin.domain.notification.infrastructure.mapper.NotificationMapper.toDomain
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.less
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.or

class NotificationRepositoryImpl : NotificationRepository {
    override suspend fun save(command: NotificationCommand): Notification =
        suspendTransaction {
            NotificationEntity
                .new {
                    this.userId = command.userId?.let { EntityID(it.value, Users) }
                    this.notiType = command.notiType
                    this.title = command.title
                    this.message = command.message
                    this.imageUrl = command.imageUrl
                    this.isBroadcast = command.isBroadcast
                    this.refId = command.refId
                    this.refType = command.refType
                }.toDomain()
        }

    override suspend fun findByUserId(
        userId: UserId,
        cursor: Long?,
        size: Int,
    ): List<Notification> =
        suspendTransaction {
            // 개인 알림 조건 + 브로드캐스트 알림 조건
            val condition = (
                (Notifications.userId eq userId.value) or
                    (Notifications.isBroadcast eq true)
            ).let { base ->
                if (cursor != null) {
                    base and (Notifications.id less cursor)
                } else {
                    base
                }
            }

            NotificationEntity
                .find { condition }
                .orderBy(Notifications.id to SortOrder.DESC)
                .limit(size)
                .map { it.toDomain() }
        }

    override suspend fun markAsRead(notificationId: NotificationId, userId: UserId): Boolean =
        suspendTransaction {
            val updated = Notifications.updateWithTimestamp({
                (Notifications.id eq notificationId.value) and
                    (Notifications.userId eq userId.value)
            }) {
                it[isRead] = true
            }
            updated > 0
        }

    override suspend fun deleteAll(userId: UserId): Unit =
        suspendTransaction {
            Notifications.deleteWhere {
                Notifications.userId eq userId.value
            }
        }
}
