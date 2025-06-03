package com.peekr.domain.auth.infrastructure.persistence

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object KeywordComments : LongIdTable("keyword_comment") {
    val userId = reference("user_id", Users)
    val keywordId = reference("keyword_id", Keywords)
    val comment = text("comment")
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    init {
        index("idx_keywordcomment_keyword_created", false, keywordId, createdAt)
    }
}
