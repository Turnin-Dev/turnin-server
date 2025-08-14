package com.peekr.common.db.schema

import com.peekr.common.util.PeekrDateTime
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

/** 리프레쉬 토큰 엔티티 클래스 (복수형) */
object RefreshTokens : Table("refresh_tokens") {
    val user = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val refreshToken = text("refresh_token").uniqueIndex("uq_refresh_tokens_token")
    val createdAt = timestamp("created_at").defaultExpression(PeekrDateTime.timestamp)
    override val primaryKey = PrimaryKey(user)
}
