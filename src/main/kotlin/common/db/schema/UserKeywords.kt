package com.turnin.common.db.schema

import com.turnin.common.db.BaseEntity
import com.turnin.common.db.BaseEntityClass
import com.turnin.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp

/** 사용자별 키워드 엔티티 클래스 (Exposed DSL 방식) */
object UserKeywords : BaseLongIdTable("user_keyword") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.RESTRICT)
    val keywordId = reference("keyword_id", Keywords, onDelete = ReferenceOption.CASCADE)
    val description = text("description").nullable()
    val isActive = bool("is_active").default(true)

    /** 작성자 쪽 사유(직접 삭제, 계정 탈퇴)로 삭제된 시각. 운영자 숨김과 구분하고 1년 후 파기하는 기준 */
    val deletedAt = timestamp("deleted_at").nullable()

    init {
        uniqueIndex("uq_user_id_keyword_id", userId, keywordId)
        index("idx_user_keyword_combo_keyword", false, keywordId, userId)
        index("idx_user_keyword_deleted_at", false, deletedAt, filterCondition = { deletedAt.isNotNull() })
    }
}

/** 사용자별 키워드 엔티티 클래스 (Exposed DAO/ORM 방식) */
class UserKeywordEntity(id: EntityID<Long>) : BaseEntity(id, UserKeywords) {
    companion object : BaseEntityClass<UserKeywordEntity>(UserKeywords)

    var userId by UserKeywords.userId
    var keywordId by UserKeywords.keywordId
    var description by UserKeywords.description
    var isActive by UserKeywords.isActive
    var deletedAt by UserKeywords.deletedAt

    val keywordEntity by KeywordEntity referencedOn UserKeywords.keywordId
}
