package com.peekr.common.db.schema

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption

object UserKeywords : BaseLongIdTable("user_keyword") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val keywordId = reference("keyword_id", Keywords, onDelete = ReferenceOption.CASCADE)
    val description = text("description").nullable()

    init {
        // TODO: 인덱스 검토
        uniqueIndex("uq_user_keyword", userId, keywordId)
        index("idx_userkeyword_keyword_user", false, keywordId, userId)
    }
}

class UserKeywordEntity(id: EntityID<Long>) : BaseEntity(id, UserKeywords) {
    companion object : BaseEntityClass<UserKeywordEntity>(UserKeywords)

    var userId by UserKeywords.userId
    var keywordId by UserKeywords.keywordId
    var description by UserKeywords.description
}
