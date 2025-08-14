package com.peekr.common.db.schema

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption

object Blocks : BaseLongIdTable("block") {
    val blockerId = reference("blocker_id", Users, onDelete = ReferenceOption.RESTRICT)
    val blockedId = reference("blocked_id", Users, onDelete = ReferenceOption.RESTRICT)
    val reasonId = reference("reason_id", BlockReasons, onDelete = ReferenceOption.RESTRICT)
    val customReason = text("custom_reason").nullable()
    val isBlocked = bool("is_blocked").default(true)

    init {
        uniqueIndex("uq_block_pair", blockerId, blockedId)
    }
}

class BlockEntity(id: EntityID<Long>) : BaseEntity(id, Blocks) {
    companion object : BaseEntityClass<BlockEntity>(Blocks)

    var blockerId by Blocks.blockerId
    var blockedId by Blocks.blockedId
    var reasonId by Blocks.reasonId
    var customReason by Blocks.customReason
    var isBlocked by Blocks.isBlocked
}
