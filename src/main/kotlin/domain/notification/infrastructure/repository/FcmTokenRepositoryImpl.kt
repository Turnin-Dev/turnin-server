package com.peekr.domain.notification.infrastructure.repository

import com.peekr.common.db.schema.UserFcmTokenEntity
import com.peekr.common.db.schema.UserFcmTokens
import com.peekr.common.db.schema.Users
import com.peekr.common.db.suspendTransaction
import com.peekr.common.db.updateWithTimestamp
import com.peekr.common.model.id.UserId
import com.peekr.domain.notification.domain.model.FcmToken
import com.peekr.domain.notification.domain.repository.FcmTokenRepository
import com.peekr.domain.notification.infrastructure.mapper.NotificationMapper.toDomain
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere

class FcmTokenRepositoryImpl : FcmTokenRepository {
    override suspend fun upsert(userId: UserId, token: String): FcmToken =
        suspendTransaction {
            val existing = UserFcmTokenEntity
                .find {
                    UserFcmTokens.token eq token
                }.firstOrNull()

            existing
                ?.apply {
                    this.userId = EntityID(userId.value, Users)
                    this.isActive = true
                }?.toDomain()
                ?: UserFcmTokenEntity
                    .new {
                        this.userId = EntityID(userId.value, Users)
                        this.token = token
                        this.isActive = true
                    }.toDomain()
        }

    override suspend fun deactivate(userId: UserId, token: String): Unit =
        suspendTransaction {
            UserFcmTokens.updateWithTimestamp({
                (UserFcmTokens.userId eq userId.value) and
                    (UserFcmTokens.token eq token)
            }) {
                it[isActive] = false
            }
        }

    override suspend fun deactivateAll(userId: UserId): Unit =
        suspendTransaction {
            UserFcmTokens.updateWithTimestamp({
                UserFcmTokens.userId eq userId.value
            }) {
                it[isActive] = false
            }
        }

    override suspend fun deleteAll(userId: UserId): Unit =
        suspendTransaction {
            UserFcmTokens.deleteWhere {
                UserFcmTokens.userId eq userId.value
            }
        }

    override suspend fun findActiveTokens(userId: UserId): List<String> =
        suspendTransaction {
            UserFcmTokenEntity
                .find {
                    (UserFcmTokens.userId eq userId.value) and
                        (UserFcmTokens.isActive eq true)
                }.map { it.token }
        }
}
