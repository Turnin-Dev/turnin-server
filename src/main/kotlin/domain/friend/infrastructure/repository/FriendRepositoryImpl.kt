package com.peekr.domain.friend.infrastructure.repository

import com.peekr.common.db.schema.FriendEntity
import com.peekr.common.db.schema.Friends
import com.peekr.common.db.schema.Users
import com.peekr.common.db.suspendTransaction
import com.peekr.common.model.FriendRequestStatus
import com.peekr.common.model.id.UserId
import com.peekr.common.util.PeekrDateTime
import com.peekr.common.util.toOffsetDateTime
import com.peekr.domain.friend.domain.model.Friend
import com.peekr.domain.friend.domain.model.FriendsPagingData
import com.peekr.domain.friend.domain.model.IncomingRequestPagingData
import com.peekr.domain.friend.domain.repository.FriendRepository
import com.peekr.domain.friend.infrastructure.mapper.FriendMapper.toDomain
import com.peekr.domain.friend.infrastructure.mapper.FriendMapper.toDomainIncomingRequester
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class FriendRepositoryImpl : FriendRepository {
    override suspend fun getFriendsPagination(
        userId: UserId,
        offset: Long,
        size: Int,
    ): FriendsPagingData = suspendTransaction {
        // 1) 친구 조회 쿼리 선언
        val friendCondition = Op.build {
            (Friends.status eq FriendRequestStatus.ACCEPTED) and
                (
                    (Friends.requesterId eq userId.value) or
                        (Friends.receiverId eq userId.value)
                )
        }

        // 2) 전체 항목(친구) 개수 조회
        val totalCount = Friends
            .select(Friends.id)
            .where(friendCondition)
            .count()

        // 3) 현재 페이지 목록 조회
        val friends = Friends
            .selectAll()
            .where(friendCondition)
            .orderBy(Friends.id to SortOrder.DESC)
            .limit(count = size)
            .offset(start = offset)
            .map { it.toDomain() }

        // 4) 결과 반환
        FriendsPagingData(totalCount, friends)
    }

    override suspend fun getIncomingRequests(
        userId: UserId,
        offset: Long,
        size: Int,
    ): IncomingRequestPagingData = suspendTransaction {
        // 1. 받은 친구 요청 조회 쿼리 선언
        val incomingRequestQuery = Op.build {
            (Friends.receiverId eq userId.value) and
                (Friends.status eq FriendRequestStatus.PENDING)
        }

        // 2. 전체 항목 개수 조회
        val totalCount = Friends
            .select(Friends.id)
            .where(incomingRequestQuery)
            .count()

        // 3. 현재 페이지 목록 조회
        val incomingRequests = Friends
            .select(
                Friends.id,
                Friends.requesterId,
                Friends.status,
                Friends.respondedAt,
                Friends.createdAt,
                Friends.updatedAt,
            ).where(incomingRequestQuery)
            .orderBy(Friends.createdAt to SortOrder.DESC)
            .limit(count = size)
            .offset(start = offset)
            .map { it.toDomainIncomingRequester() }

        // 4. 결과 반환
        IncomingRequestPagingData(totalCount, incomingRequests)
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
