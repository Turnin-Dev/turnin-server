package com.peekr.common.db.schema

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import com.peekr.common.db.DatabaseUtils.customPostgresEnum
import com.peekr.common.db.DatabaseUtils.timestamptz
import com.peekr.common.model.FriendRequestStatus
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption

/** 친구 엔티티 클래스 (Exposed DSL 방식) */
object Friends : BaseLongIdTable("friend") {
    val requesterId = reference("requester_id", Users, onDelete = ReferenceOption.RESTRICT)
    val receiverId = reference("receiver_id", Users, onDelete = ReferenceOption.RESTRICT)
    val status = customPostgresEnum<FriendRequestStatus>("status", "friend_status").default(FriendRequestStatus.PENDING)
    val respondedAt = timestamptz("responded_at").nullable()

    init {
        // 중복 친구 요청 방지
        uniqueIndex("uq_friend_requester_receiver", requesterId, receiverId)
        // 친구 관계 조회를 위한 인덱스
        index("idx_friend_receiver_status", false, receiverId, status)
        // 자기 자신에게 친구 요청 방지
        check("chk_friend_not_self") { requesterId neq receiverId }
    }
}

/** 친구 엔티티 클래스 (Exposed DAO/ORM 방식) */
class FriendEntity(id: EntityID<Long>) : BaseEntity(id, Friends) {
    companion object : BaseEntityClass<FriendEntity>(Friends)

    var requesterId by Friends.requesterId
    var receiverId by Friends.receiverId
    var status by Friends.status
    var respondedAt by Friends.respondedAt
}
