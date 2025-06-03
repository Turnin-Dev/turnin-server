package com.peekr.domain.auth.infrastructure.persistence

import com.peekr.domain.friend.domain.value.FriendStatus
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object Friends : LongIdTable("friend") {
    val requesterId = reference("requester_id", Users)
    val receiverId = reference("receiver_id", Users)
    val status = enumerationByName("status", 10, FriendStatus::class).default(FriendStatus.PENDING)
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)
    val respondedAt = timestamp("responded_at").nullable()

    init {
        index("idx_friend_requester_receiver", false, requesterId, receiverId)
        index("idx_friend_receiver_requester", false, receiverId, requesterId)
        index("idx_friend_status", false, status)
    }
}
