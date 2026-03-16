package com.peekr.common.db.schema

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption

/** 사용자별 키워드 엔티티 클래스 (Exposed DSL 방식) */
object UserKeywords : BaseLongIdTable("user_keyword") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.RESTRICT)
    val keywordId = reference("keyword_id", Keywords, onDelete = ReferenceOption.CASCADE)
    val description = text("description").nullable()
    val isActive = bool("is_active").default(true)

    init {
        uniqueIndex("uq_user_id_keyword_id", userId, keywordId)
        index("idx_user_keyword_combo_keyword", false, keywordId, userId)
    }
}

/** 사용자별 키워드 엔티티 클래스 (Exposed DAO/ORM 방식) */
class UserKeywordEntity(id: EntityID<Long>) : BaseEntity(id, UserKeywords) {
    companion object : BaseEntityClass<UserKeywordEntity>(UserKeywords)

    var userId by UserKeywords.userId
    var keywordId by UserKeywords.keywordId
    var description by UserKeywords.description
    var isActive by UserKeywords.isActive

    val keywordEntity by KeywordEntity referencedOn UserKeywords.keywordId
}
