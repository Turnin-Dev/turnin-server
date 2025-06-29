package com.peekr.common.db.scheme

import org.jetbrains.exposed.sql.Table

object RefreshTokens : Table("refresh_tokens") {
    val user = reference("user_id", Users).uniqueIndex()
    val refreshToken = text("refresh_token")
    override val primaryKey = PrimaryKey(user)
}
