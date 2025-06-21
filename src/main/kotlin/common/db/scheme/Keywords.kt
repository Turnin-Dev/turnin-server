package com.peekr.common.db.scheme

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID

object Keywords : BaseLongIdTable("keyword") {
    val keyword = varchar("keyword", 100).uniqueIndex()
    val createdBy = reference("created_by", Users)

    init {
        index("idx_keyword_created_by", false, createdBy)
    }
}

class Keyword(id: EntityID<Long>) : BaseEntity(id, Keywords) {
    companion object : BaseEntityClass<Keyword>(Keywords)

    var keyword by Keywords.keyword
    var createdBy by Keywords.createdBy
}
