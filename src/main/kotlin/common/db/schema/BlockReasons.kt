package com.peekr.common.db.schema

import com.peekr.common.db.BaseEntityWithoutTimestamp
import com.peekr.common.db.BaseLongIdTableWithoutTimestamp
import org.jetbrains.exposed.dao.id.EntityID

object BlockReasons : BaseLongIdTableWithoutTimestamp("block_reason") {
    val code = varchar("code", 50).uniqueIndex()
    val description = text("description")
}

class BlockReasonEntity(id: EntityID<Long>) : BaseEntityWithoutTimestamp(id) {
    var code by BlockReasons.code
    var description by BlockReasons.description
}
