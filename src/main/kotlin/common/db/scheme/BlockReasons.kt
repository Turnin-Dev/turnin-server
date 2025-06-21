package com.peekr.common.db.scheme

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID

object BlockReasons : BaseLongIdTable("block_reason") {
    val code = varchar("code", 50).uniqueIndex()
    val description = text("description")
}

class BlockReasonEntity(id: EntityID<Long>) : BaseEntity(id, BlockReasons) {
    companion object : BaseEntityClass<BlockReasonEntity>(BlockReasons)

    var code by BlockReasons.code
    var description by BlockReasons.description
}
