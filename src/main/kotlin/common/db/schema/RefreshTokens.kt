package com.peekr.common.db.schema

import com.peekr.common.db.DatabaseUtils.timestamptz
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

/** 리프레쉬 토큰 엔티티 클래스 (복수형) */
object RefreshTokens : Table("refresh_tokens") {
    val user = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val refreshToken = text("refresh_token").uniqueIndex("uq_refresh_tokens_token")
    val createdAt = timestamptz("created_at")
    override val primaryKey = PrimaryKey(user)
}
