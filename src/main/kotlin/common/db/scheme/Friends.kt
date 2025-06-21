package com.peekr.common.db.scheme

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import com.peekr.domain.friend.domain.value.FriendStatus
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.javatime.timestamp

object Friends : BaseLongIdTable("friend") {
    val requesterId = reference("requester_id", Users)
    val receiverId = reference("receiver_id", Users)
    val status = enumerationByName("status", 10, FriendStatus::class).default(FriendStatus.PENDING)
    val respondedAt = timestamp("responded_at").nullable()

    init {
        index("idx_friend_requester_receiver", false, requesterId, receiverId)
        index("idx_friend_receiver_requester", false, receiverId, requesterId)
        index("idx_friend_status", false, status)
    }
}

class Friend(id: EntityID<Long>) : BaseEntity(id, Friends) {
    companion object : BaseEntityClass<Friend>(Friends)

    var requesterId by Friends.requesterId
    var receiverId by Friends.receiverId
    var status by Friends.status
    var respondedAt by Friends.respondedAt
}
