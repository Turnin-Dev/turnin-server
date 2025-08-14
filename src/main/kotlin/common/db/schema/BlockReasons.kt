package com.peekr.common.db.schema

import com.peekr.common.db.BaseEntityWithoutTimestamp
import com.peekr.common.db.BaseLongIdTableWithoutTimestamp
import org.jetbrains.exposed.dao.id.EntityID

/** 차단 사유 엔티티 클래스 (복수형) */
object BlockReasons : BaseLongIdTableWithoutTimestamp("block_reason") {
    val code = varchar("code", 50).uniqueIndex()
    val description = text("description")
}

/** 차단 사유 엔티티 클래스 (단수형) */
class BlockReasonEntity(id: EntityID<Long>) : BaseEntityWithoutTimestamp(id) {
    var code by BlockReasons.code
    var description by BlockReasons.description
}
