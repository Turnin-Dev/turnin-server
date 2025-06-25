package com.peekr.common.db.scheme

import com.peekr.common.util.PeekrDateTime
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object RefreshTokens : Table("refresh_tokens") {
    val user = reference("user_id", Users).uniqueIndex()
    val refreshToken = text("refresh_token")
    val expiresAt = timestamp("expires_at")
    val createdAt = timestamp("created_at").defaultExpression(PeekrDateTime.timestamp)
    override val primaryKey = PrimaryKey(user)
}
