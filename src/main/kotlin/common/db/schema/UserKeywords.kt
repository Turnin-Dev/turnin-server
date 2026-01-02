package com.peekr.common.db.schema

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption

/** 사용자별 키워드 엔티티 클래스 (복수형) */
object UserKeywords : BaseLongIdTable("user_keyword") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.RESTRICT)
    val keywordId = reference("keyword_id", Keywords, onDelete = ReferenceOption.CASCADE)
    val description = text("description").nullable()

    init {
        uniqueIndex("uq_userkeyword_user_keyword", userId, keywordId)
        index("idx_user_keyword_user_id", false, userId)
    }
}

/** 사용자별 키워드 엔티티 클래스 (단수형) */
class UserKeywordEntity(id: EntityID<Long>) : BaseEntity(id, UserKeywords) {
    companion object : BaseEntityClass<UserKeywordEntity>(UserKeywords)

    var userId by UserKeywords.userId
    var keywordId by UserKeywords.keywordId
    var description by UserKeywords.description

    val keywordEntity by KeywordEntity referencedOn UserKeywords.keywordId
}
