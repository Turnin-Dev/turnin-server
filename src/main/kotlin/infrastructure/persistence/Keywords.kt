package com.peekr.infrastructure.persistence

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object Keywords : LongIdTable("keyword") {
    val keyword = varchar("keyword", 100).uniqueIndex()
    val createdBy = reference("created_by", Users)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    init {
        index("idx_keyword_created_by", false, createdBy)
    }
}
