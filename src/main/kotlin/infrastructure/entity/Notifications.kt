package com.peekr.infrastructure.entity

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object Notifications : LongIdTable("notification") {
    val userId = reference("user_id", Users)
    val notiType = varchar("noti_type", 50)
    val message = text("message")
    val isRead = bool("is_read").default(false)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
}
