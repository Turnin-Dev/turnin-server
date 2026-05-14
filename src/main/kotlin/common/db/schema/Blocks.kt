package com.turnin.common.db.schema

import com.turnin.common.db.BaseEntity
import com.turnin.common.db.BaseEntityClass
import com.turnin.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption

/** 차단 엔티티 클래스 (Exposed DSL 방식) */
object Blocks : BaseLongIdTable("block") {
    val blockerId = reference("blocker_id", Users, onDelete = ReferenceOption.RESTRICT)
    val blockedId = reference("blocked_id", Users, onDelete = ReferenceOption.RESTRICT)
    val reasonId = reference("reason_id", BlockReasons, onDelete = ReferenceOption.RESTRICT)
    val customReason = text("custom_reason").nullable()

    init {
        uniqueIndex("uq_block_pair", blockerId, blockedId)
        check("chk_block_not_self") { blockerId neq blockedId }
    }
}

/** 차단 엔티티 클래스 (Exposed DAO/ORM 방식) */
class BlockEntity(id: EntityID<Long>) : BaseEntity(id, Blocks) {
    companion object : BaseEntityClass<BlockEntity>(Blocks)

    var blockerId by Blocks.blockerId
    var blockedId by Blocks.blockedId
    var reasonId by Blocks.reasonId
    var customReason by Blocks.customReason
}
