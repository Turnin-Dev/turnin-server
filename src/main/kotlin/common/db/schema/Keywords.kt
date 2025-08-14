package com.peekr.common.db.schema

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption

/** 키워드 엔티티 클래스 (복수형) */
object Keywords : BaseLongIdTable("keyword") {
    val keyword = varchar("keyword", 100).uniqueIndex()
    val createdBy = reference("created_by", Users, onDelete = ReferenceOption.RESTRICT)
}

/** 키워드 엔티티 클래스 (단수형) */
class KeywordEntity(id: EntityID<Long>) : BaseEntity(id, Keywords) {
    companion object : BaseEntityClass<KeywordEntity>(Keywords)

    var keyword by Keywords.keyword
    var createdBy by Keywords.createdBy
}
