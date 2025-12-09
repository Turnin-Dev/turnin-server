package com.peekr.domain.friend.infrastructure.repository

import com.peekr.common.db.schema.FriendEntity
import com.peekr.common.db.schema.Friends
import com.peekr.common.db.schema.Users
import com.peekr.common.db.suspendTransaction
import com.peekr.common.model.FriendRequestStatus
import com.peekr.common.model.id.UserId
import com.peekr.common.util.PeekrDateTime
import com.peekr.common.util.pagination.PaginationParams
import com.peekr.common.util.toOffsetDateTime
import com.peekr.domain.friend.domain.model.Friend
import com.peekr.domain.friend.domain.repository.FriendRepository
import com.peekr.domain.friend.infrastructure.mapper.FriendMapper.toDomain
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

// TODO: 친구 기능은 이미 취소된 즉, 이미 데이터 지워진 상태에서 쿼리될 확률이 높다. -> 대처해야함
class FriendRepositoryImpl : FriendRepository {
    override suspend fun getFriendsPagination(
        userId: UserId,
        paginationParams: PaginationParams,
    ): List<Friend> = suspendTransaction {
        val friendCondition = Op.Companion.build {
            (Friends.status eq FriendRequestStatus.ACCEPTED) and
                (
                    (Friends.requesterId eq userId.value) or
                        (Friends.receiverId eq userId.value)
                )
        }
        Friends
            .selectAll()
            .where(friendCondition)
            .limit(count = paginationParams.size)
            .offset(start = paginationParams.offset)
            .map { it.toDomain() }
    }

    override suspend fun findByIds(
        userId: UserId,
        otherUserId: UserId,
    ): Friend? = suspendTransaction {
        Friends
            .selectAll()
            .where(
                ((Friends.requesterId eq userId.value) and (Friends.receiverId eq otherUserId.value)) or
                    ((Friends.requesterId eq otherUserId.value) and (Friends.receiverId eq userId.value)),
            ).singleOrNull()
            ?.toDomain()
    }

    override suspend fun countFriends(userId: UserId): Long = suspendTransaction {
        FriendEntity.count(
            (
                ((Friends.requesterId eq userId.value) or (Friends.receiverId eq userId.value)) and
                    (Friends.status eq FriendRequestStatus.ACCEPTED)
            ),
        )
    }

    override suspend fun createFriend(
        requesterId: UserId,
        receiverId: UserId,
    ): Friend = suspendTransaction {
        val savedFriend = FriendEntity.new {
            this.requesterId = EntityID(requesterId.value, Users)
            this.receiverId = EntityID(receiverId.value, Users)
            this.status = FriendRequestStatus.PENDING
            this.respondedAt = null
        }

        savedFriend.toDomain()
    }

    override suspend fun updateFriendRequestStatus(
        userId1: UserId,
        userId2: UserId,
        requestStatus: FriendRequestStatus,
    ): Boolean = suspendTransaction {
        val updateCondition = Op.build {
            ((Friends.requesterId eq userId1.value) and (Friends.receiverId eq userId2.value)) or
                ((Friends.requesterId eq userId2.value) and (Friends.receiverId eq userId1.value))
        }
        Friends.update({ updateCondition }) {
            it[this.status] = requestStatus
            it[this.respondedAt] = PeekrDateTime.now().toOffsetDateTime()
        } > 0
    }

    override suspend fun deleteFriend(
        userId1: UserId,
        userId2: UserId,
    ): Boolean = suspendTransaction {
        Friends.deleteWhere {
            ((Friends.requesterId eq userId1.value) and (Friends.receiverId eq userId2.value)) or
                ((Friends.requesterId eq userId2.value) and (Friends.receiverId eq userId1.value))
        } > 0
    }
}
