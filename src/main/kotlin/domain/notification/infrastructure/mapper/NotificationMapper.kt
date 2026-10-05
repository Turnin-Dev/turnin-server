package com.turnin.domain.notification.infrastructure.mapper

import com.turnin.common.db.schema.NotificationEntity
import com.turnin.common.db.schema.UserFcmTokenEntity
import com.turnin.common.model.id.FcmTokenId
import com.turnin.common.model.id.NotificationId
import com.turnin.common.model.id.UserId
import com.turnin.domain.notification.domain.model.FcmToken
import com.turnin.domain.notification.domain.model.Notification
import kotlinx.serialization.json.Json

object NotificationMapper {
    /** 딥링크용 부가 데이터([Notification.refData]) JSON 문자열 직렬화 */
    fun Map<String, String>.toRefDataJson(): String = Json.encodeToString(this)

    private fun String.toRefData(): Map<String, String> = Json.decodeFromString(this)

    fun NotificationEntity.toDomain() = Notification(
        id = NotificationId(this.id.value),
        userId = this.userId?.let { UserId(it.value) },
        notiType = this.notiType,
        title = this.title,
        message = this.message,
        imageUrl = this.imageUrl,
        isRead = this.isRead,
        isBroadcast = this.isBroadcast,
        refId = this.refId,
        refType = this.refType,
        refData = this.refData?.toRefData(),
        createdAt = this.createdAt.toEpochSecond(),
        updatedAt = this.updatedAt.toEpochSecond(),
    )

    fun UserFcmTokenEntity.toDomain() = FcmToken(
        id = FcmTokenId(this.id.value),
        userId = UserId(this.userId.value),
        token = this.token,
        isActive = this.isActive,
        createdAt = this.createdAt.toEpochSecond(),
        updatedAt = this.updatedAt.toEpochSecond(),
    )
}
