package com.peekr.common.db.schema

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID

object KeywordComments : BaseLongIdTable("keyword_comment") {
    val userId = reference("user_id", Users)
    val keywordId = reference("keyword_id", Keywords)
    val comment = text("comment")

    init {
        index("idx_keywordcomment_keyword_created", false, keywordId, createdAt)
    }
}

class KeywordCommentEntity(id: EntityID<Long>) : BaseEntity(id, KeywordComments) {
    companion object : BaseEntityClass<KeywordCommentEntity>(KeywordComments)

    var userId by KeywordComments.userId
    var keywordId by KeywordComments.keywordId
    var comment by KeywordComments.comment
}
