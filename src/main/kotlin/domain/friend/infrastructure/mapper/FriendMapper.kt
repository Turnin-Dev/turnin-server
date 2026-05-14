package com.turnin.domain.friend.infrastructure.mapper

import com.turnin.common.db.schema.FriendEntity
import com.turnin.common.db.schema.Friends
import com.turnin.common.model.id.FriendId
import com.turnin.common.model.id.UserId
import com.turnin.domain.friend.domain.model.Friend
import com.turnin.domain.friend.domain.model.IncomingRequest
import org.jetbrains.exposed.sql.ResultRow

object FriendMapper {
    fun ResultRow.toDomain(): Friend =
        Friend(
            id = FriendId(this[Friends.id].value),
            requesterId = UserId(this[Friends.requesterId].value),
            receiverId = UserId(this[Friends.receiverId].value),
            requestStatus = this[Friends.status],
            respondedAt = this[Friends.respondedAt]?.toEpochSecond(),
            createdAt = this[Friends.createdAt].toEpochSecond(),
            updatedAt = this[Friends.updatedAt].toEpochSecond(),
        )

    fun ResultRow.toDomainIncomingRequester(): IncomingRequest =
        IncomingRequest(
            id = FriendId(this[Friends.id].value),
            requesterId = UserId(this[Friends.requesterId].value),
            requestStatus = this[Friends.status],
            respondedAt = this[Friends.respondedAt]?.toEpochSecond(),
            createdAt = this[Friends.createdAt].toEpochSecond(),
            updatedAt = this[Friends.updatedAt].toEpochSecond(),
        )

    fun FriendEntity.toDomain(): Friend =
        Friend(
            id = FriendId(this.id.value),
            requesterId = UserId(this.requesterId.value),
            receiverId = UserId(this.receiverId.value),
            requestStatus = this.status,
            respondedAt = this.respondedAt?.toEpochSecond(),
            createdAt = this.createdAt.toEpochSecond(),
            updatedAt = this.updatedAt.toEpochSecond(),
        )
}
