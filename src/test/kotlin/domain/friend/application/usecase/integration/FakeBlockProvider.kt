package com.peekr.domain.friend.application.usecase.integration

import com.peekr.common.db.schema.Blocks
import com.peekr.common.db.suspendTransaction
import com.peekr.common.model.id.UserId
import com.peekr.domain.friend.domain.provider.BlockProvider
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.intLiteral
import org.jetbrains.exposed.sql.or

class FakeBlockProvider : BlockProvider {
    override suspend fun isBlockedRelationship(
        userId1: UserId,
        userId2: UserId,
    ): Boolean = suspendTransaction {
        if (userId1 == userId2) return@suspendTransaction false

        Blocks
            .select(intLiteral(1))
            .where {
                (Blocks.blockerId eq userId1.value and (Blocks.blockedId eq userId2.value)) or
                    (Blocks.blockerId eq userId2.value and (Blocks.blockedId eq userId1.value))
            }.limit(1)
            .any()
    }
}
