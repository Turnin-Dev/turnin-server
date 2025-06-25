package com.peekr.common.db.scheme

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID

object UserKeywords : BaseLongIdTable("user_keyword") {
    val userId = reference("user_id", Users)
    val keywordId = reference("keyword_id", Keywords)
    val description = text("description").nullable()

    init {
        // TODO: 인덱스 검토
        uniqueIndex("unique_user_keyword", userId, keywordId)
        index("idx_userkeyword_keyword_user", false, keywordId, userId)
    }
}

class UserKeywordEntity(id: EntityID<Long>) : BaseEntity(id, UserKeywords) {
    companion object : BaseEntityClass<UserKeywordEntity>(UserKeywords)

    var userId by UserKeywords.userId
    var keywordId by UserKeywords.keywordId
    var description by UserKeywords.description
}
