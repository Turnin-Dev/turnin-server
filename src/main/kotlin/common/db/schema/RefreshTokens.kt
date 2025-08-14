package com.peekr.common.db.schema

import com.peekr.common.util.PeekrDateTime
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp

object RefreshTokens : Table("refresh_tokens") {
    val user = reference("user_id", Users, onDelete = ReferenceOption.CASCADE)
    val refreshToken = text("refresh_token").uniqueIndex("uq_refresh_tokens_token")
    val createdAt = timestamp("created_at").defaultExpression(PeekrDateTime.timestamp)
    override val primaryKey = PrimaryKey(user)

    init {
        // 추후 만료 토큰 일괄 정리를 위해 인덱스 생성
        index("idx_refresh_tokens_created_at", false, createdAt)
    }
}
