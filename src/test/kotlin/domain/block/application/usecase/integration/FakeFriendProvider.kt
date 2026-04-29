package com.turnin.domain.block.application.usecase.integration

import com.turnin.common.db.schema.Friends
import com.turnin.common.db.suspendTransaction
import com.turnin.common.model.id.UserId
import com.turnin.domain.block.domain.provider.FriendProvider
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.or

class FakeFriendProvider : FriendProvider {
    override suspend fun deleteFriend(
        userId1: UserId,
        userId2: UserId,
    ): Boolean = suspendTransaction {
        Friends.deleteWhere {
            ((Friends.requesterId eq userId1.value) and (Friends.receiverId eq userId2.value)) or
                ((Friends.requesterId eq userId2.value) and (Friends.receiverId eq userId1.value))
        } > 0
    }
}
