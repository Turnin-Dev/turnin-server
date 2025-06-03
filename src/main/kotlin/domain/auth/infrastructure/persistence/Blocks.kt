package com.peekr.domain.auth.infrastructure.persistence

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object Blocks : LongIdTable("block") {
    val blockerId = reference("blocker_id", Users)
    val blockedId = reference("blocked_id", Users)
    val reasonId = reference("reason_id", BlockReasons)
    val customReason = text("custom_reason").nullable()
    val isBlocked = bool("is_blocked").default(true)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val updatedAt = timestamp("updated_at").defaultExpression(CurrentTimestamp)

    init {
        index("idx_block_pair", false, blockerId, blockedId)
    }
}
