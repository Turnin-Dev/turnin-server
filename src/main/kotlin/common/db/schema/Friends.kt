package com.peekr.common.db.schema

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import com.peekr.common.db.DatabaseUtils.customPostgresEnum
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.javatime.timestamp

object Friends : BaseLongIdTable("friend") {
    val requesterId = reference("requester_id", Users, onDelete = ReferenceOption.CASCADE)
    val receiverId = reference("receiver_id", Users, onDelete = ReferenceOption.CASCADE)
    val status = customPostgresEnum<FriendStatus>("status", sqlName = "friend_status").default(FriendStatus.PENDING)
    val respondedAt = timestamp("responded_at").nullable()

    init {
        // 중복 친구 요청 방지
        uniqueIndex("uq_friend_requester_receiver", requesterId, receiverId)
        index("idx_friend_receiver_requester", false, receiverId, requesterId)
        index("idx_friend_status", false, status)
        // 자기 자신에게 친구 요청 방지
        check("chk_friend_not_self") { requesterId neq receiverId }
    }
}

class FriendEntity(id: EntityID<Long>) : BaseEntity(id, Friends) {
    companion object : BaseEntityClass<FriendEntity>(Friends)

    var requesterId by Friends.requesterId
    var receiverId by Friends.receiverId
    var status by Friends.status
    var respondedAt by Friends.respondedAt
}
