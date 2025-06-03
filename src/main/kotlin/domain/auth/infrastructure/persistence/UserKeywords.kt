package com.peekr.domain.auth.infrastructure.persistence

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object UserKeywords : LongIdTable("user_keyword") {
    val userId = reference("user_id", Users)
    val keywordId = reference("keyword_id", Keywords)
    val description = text("description").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    init {
        // TODO: 인덱스 검토
        uniqueIndex("unique_user_keyword", userId, keywordId)
        index("idx_userkeyword_keyword_user", false, keywordId, userId)
    }
}
