package com.peekr.domain.user.infrastructure.repository.impl

import com.peekr.common.db.SubCountQueryFunction
import com.peekr.common.db.schema.FriendStatus
import com.peekr.common.db.schema.Friends
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.db.schema.Users
import com.peekr.common.db.suspendTransaction
import com.peekr.common.model.Introduce
import com.peekr.common.model.UserId
import com.peekr.domain.user.domain.model.User
import com.peekr.domain.user.domain.model.UserPatch
import com.peekr.domain.user.domain.model.UserProfile
import com.peekr.domain.user.domain.repository.UserRepository
import com.peekr.domain.user.infrastructure.mapper.UserMapper
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.update

// TODO: Friend 테이블 조작은 Provider로 빼야함
class UserRepositoryImpl : UserRepository {
    override suspend fun findById(id: UserId): User? = suspendTransaction {
        UserEntity.findById(id.value)?.let { entity ->
            UserMapper.toDomain(entity)
        }
    }

    override suspend fun findUserProfileById(id: UserId): UserProfile? = suspendTransaction {
        val friendCountAlias = Friends.alias("friendCountAlias")
        val friendsCount = SubCountQueryFunction(
            table = friendCountAlias,
            where = {
                (friendCountAlias[Friends.receiverId] eq id.value) and
                    (friendCountAlias[Friends.status] eq FriendStatus.ACCEPTED)
            },
        ).alias("friendsCount")

        Users
            .select(Users.columns + friendsCount)
            .where(Users.id eq id.value)
            .map { row ->
                val user = UserMapper.toDomain(row)
                UserMapper.toDomain(user, row[friendsCount])
            }.singleOrNull()
    }

    override suspend fun update(
        userId: UserId,
        patch: UserPatch,
    ): Boolean = suspendTransaction {
        Users.update({ (Users.id eq userId.value) }) { row ->
            row[displayId] = patch.displayId.value
            row[name] = patch.name.value
            row[profileImageUrl] = patch.profileImageUrl
            patch.introduce?.let { row[introduce] = it.value }
            patch.profileImageUrl?.let { row[profileImageUrl] = it }
        } > 0
    }

    override suspend fun updateIntroduce(
        userId: UserId,
        patchIntroduce: Introduce,
    ): Boolean = suspendTransaction {
        Users.update({ (Users.id eq userId.value) }) { row ->
            row[introduce] = patchIntroduce.value
        } > 0
    }
}
