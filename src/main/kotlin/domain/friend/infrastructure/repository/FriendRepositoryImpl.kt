package com.peekr.domain.friend.infrastructure.repository

import com.peekr.common.db.DatabaseUtils.eqEnum
import com.peekr.common.db.extension.existsUser
import com.peekr.common.db.extension.filterActiveUser
import com.peekr.common.db.schema.Blocks
import com.peekr.common.db.schema.FriendEntity
import com.peekr.common.db.schema.Friends
import com.peekr.common.db.schema.Users
import com.peekr.common.db.suspendTransaction
import com.peekr.common.db.updateWithTimestamp
import com.peekr.common.model.FriendRequestStatus
import com.peekr.common.model.UserName
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.UserId
import com.peekr.common.util.PeekrDateTime
import com.peekr.common.util.toOffsetDateTime
import com.peekr.domain.friend.domain.model.Friend
import com.peekr.domain.friend.domain.model.FriendFcmContext
import com.peekr.domain.friend.domain.model.FriendRequestContext
import com.peekr.domain.friend.domain.model.FriendsPagingData
import com.peekr.domain.friend.domain.model.IncomingRequestPagingData
import com.peekr.domain.friend.domain.model.UserInfo
import com.peekr.domain.friend.domain.repository.FriendRepository
import com.peekr.domain.friend.infrastructure.mapper.FriendMapper.toDomain
import com.peekr.domain.friend.infrastructure.mapper.FriendMapper.toDomainIncomingRequester
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.LongColumnType
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.leftJoin
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.TransactionManager

class FriendRepositoryImpl : FriendRepository {
    override suspend fun getFriendsPagination(
        userId: UserId,
        offset: Long,
        size: Int,
    ): FriendsPagingData = suspendTransaction {
        // 1) 친구 조회 쿼리 선언
        val friendCondition = Op.build {
            (Friends.status eqEnum FriendRequestStatus.ACCEPTED) and
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
                (Friends.status eqEnum FriendRequestStatus.PENDING)
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
                    (Friends.status eqEnum FriendRequestStatus.ACCEPTED)
            ),
        )
    }

    override suspend fun getFriendRequestContext(
        requesterId: UserId,
        receiverId: UserId,
    ): FriendRequestContext? = suspendTransaction {
        // 사용자 정보 + 차단 관계 한 번에 조회 (LEFT JOIN)
        val rows = Users
            .leftJoin(
                otherTable = Blocks,
                onColumn = { Users.id },
                otherColumn = { Blocks.blockerId },
                additionalConstraint = {
                    (
                        (Blocks.blockerId eq requesterId.value) and
                            (Blocks.blockedId eq receiverId.value)
                    ) or (
                        (Blocks.blockerId eq receiverId.value) and
                            (Blocks.blockedId eq requesterId.value)
                    )
                },
            ).select(
                Users.id,
                Users.displayId,
                Users.name,
                Users.profileImageUrl,
                Blocks.id,
            ).where {
                (Users.id inList listOf(requesterId.value, receiverId.value)) and
                    (Users.isActive eq true)
            }.toList()

        // 요청자/수신자 정보 파싱
        val requesterRow = rows.find { it[Users.id].value == requesterId.value }
            ?: return@suspendTransaction null
        val receiverRow = rows.find { it[Users.id].value == receiverId.value }
            ?: return@suspendTransaction null

        // 차단 관계 여부 확인
        val isBlocked = rows.any { row ->
            runCatching { row[Blocks.id] }.getOrNull() != null
        }

        FriendRequestContext(
            requesterInfo = UserInfo(
                userId = UserId(requesterRow[Users.id].value),
                displayId = DisplayId(requesterRow[Users.displayId]),
                userName = UserName(requesterRow[Users.name]),
                profileImageUrl = requesterRow[Users.profileImageUrl],
            ),
            receiverInfo = UserInfo(
                userId = UserId(receiverRow[Users.id].value),
                displayId = DisplayId(receiverRow[Users.displayId]),
                userName = UserName(receiverRow[Users.name]),
                profileImageUrl = receiverRow[Users.profileImageUrl],
            ),
            isBlocked = isBlocked,
        )
    }

    override suspend fun getFriendFcmContext(userId: UserId): FriendFcmContext = suspendTransaction {
        val sql = """
        SELECT DISTINCT ON (uft.user_id) uft.token, sender.name as sender_name
        FROM user_fcm_token uft
        INNER JOIN (
            SELECT receiver_id as friend_id FROM friend
            WHERE requester_id = ? AND status = 'ACCEPTED'
            UNION
            SELECT requester_id as friend_id FROM friend
            WHERE receiver_id = ? AND status = 'ACCEPTED'
        ) friends ON uft.user_id = friends.friend_id
        CROSS JOIN (SELECT name FROM "user" WHERE id = ?) sender
        WHERE uft.is_active = true
        ORDER BY uft.user_id, uft.updated_at DESC
        LIMIT 500
        """.trimIndent()

        val params = listOf(
            LongColumnType() to userId.value,
            LongColumnType() to userId.value,
            LongColumnType() to userId.value,
        )

        var senderName = ""
        val tokens = mutableListOf<String>()

        TransactionManager.current().exec(sql, params) { rs ->
            while (rs.next()) {
                if (senderName.isEmpty()) {
                    senderName = rs.getString("sender_name") ?: ""
                }
                tokens.add(rs.getString("token"))
            }
        }

        FriendFcmContext(
            friendTokens = tokens,
            senderName = senderName,
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
        Friends.updateWithTimestamp({ updateCondition }) {
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

    override suspend fun deleteAll(userId: UserId): Unit = suspendTransaction {
        Friends.deleteWhere {
            (Friends.requesterId eq userId.value) or
                (Friends.receiverId eq userId.value)
        }
    }

    override suspend fun existsUser(userId: UserId): Boolean = suspendTransaction {
        Users.existsUser(userId)
    }

    override suspend fun getUserInfos(userIds: List<UserId>): List<UserInfo> = suspendTransaction {
        Users
            .select(
                Users.id,
                Users.displayId,
                Users.name,
                Users.profileImageUrl,
            ).where { Users.id inList userIds.map { it.value } }
            .filterActiveUser()
            .map {
                UserInfo(
                    userId = UserId(it[Users.id].value),
                    displayId = DisplayId(it[Users.displayId]),
                    userName = UserName(it[Users.name]),
                    profileImageUrl = it[Users.profileImageUrl],
                )
            }
    }
}
