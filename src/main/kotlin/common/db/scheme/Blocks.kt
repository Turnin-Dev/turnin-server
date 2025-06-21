package com.peekr.common.db.scheme

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID

object Blocks : BaseLongIdTable("block") {
    val blockerId = reference("blocker_id", Users)
    val blockedId = reference("blocked_id", Users)
    val reasonId = reference("reason_id", BlockReasons)
    val customReason = text("custom_reason").nullable()
    val isBlocked = bool("is_blocked").default(true)

    init {
        index("idx_block_pair", false, blockerId, blockedId)
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
