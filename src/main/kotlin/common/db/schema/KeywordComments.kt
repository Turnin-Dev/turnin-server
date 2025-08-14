package com.peekr.common.db.schema

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption

/** 키워드 댓글 엔티티 클래스 (복수형) */
object KeywordComments : BaseLongIdTable("keyword_comment") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.RESTRICT)
    val userKeywordId = reference("user_keyword_id", UserKeywords, onDelete = ReferenceOption.CASCADE)
    val comment = text("comment")

    init {
        // 키워드 상세(키워드별 댓글 나열) 조회가 빈번할 것으로 예상되어 조합 인덱스 추가
        index("idx_keywordcomment_keyword_created", false, userKeywordId, createdAt)
    }
}

/** 키워드 댓글 엔티티 클래스 (단수형) */
class KeywordCommentEntity(id: EntityID<Long>) : BaseEntity(id, KeywordComments) {
    companion object : BaseEntityClass<KeywordCommentEntity>(KeywordComments)

    var userId by KeywordComments.userId
    var userKeywordId by KeywordComments.userKeywordId
    var comment by KeywordComments.comment
}
