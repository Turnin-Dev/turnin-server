package com.peekr.common.db.schema

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption

object KeywordComments : BaseLongIdTable("keyword_comment") {
    val userId = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val keywordId = reference("keyword_id", Keywords, onDelete = ReferenceOption.CASCADE)
    val comment = text("comment")

    init {
        // 키워드 상세(키워드별 댓글 나열) 조회가 빈번할 것으로 예상되어 조합 인덱스 추가
        index("idx_keywordcomment_keyword_created", false, keywordId, createdAt)
        // 사용자별 댓글 목록(예: 마이페이지) 조회가 빈번할 것으로 예상되어 userId, createdAt 조합 인덱스 추가
        index("idx_keywordcomment_user_created", false, userId, createdAt)
    }
}

class KeywordCommentEntity(id: EntityID<Long>) : BaseEntity(id, KeywordComments) {
    companion object : BaseEntityClass<KeywordCommentEntity>(KeywordComments)

    var userId by KeywordComments.userId
    var keywordId by KeywordComments.keywordId
    var comment by KeywordComments.comment
}
