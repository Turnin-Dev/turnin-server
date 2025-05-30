package com.peekr.infrastructure.entity

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object Users : LongIdTable("user") {
    val provider = enumerationByName("provider", 50, SocialLoginProvider::class)
    val providerId = varchar("provider_id", 255)
    val name = varchar("name", 50).nullable()
    val nickname = varchar("nickname", 50).nullable()
    val profileImageUrl = varchar("profile_image_url", 500).nullable()
    val introduce = text("introduce").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    init {
        uniqueIndex("unique_provider_user", provider, providerId)
    }
}
