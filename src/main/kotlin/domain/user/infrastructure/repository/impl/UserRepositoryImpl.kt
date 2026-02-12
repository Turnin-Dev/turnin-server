package com.peekr.domain.user.infrastructure.repository.impl

import com.peekr.common.db.DatabaseUtils.isNotBlockedRelationship
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.db.schema.Users
import com.peekr.common.db.suspendTransaction
import com.peekr.common.model.Introduce
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.UserId
import com.peekr.domain.user.domain.model.User
import com.peekr.domain.user.domain.model.UserPatch
import com.peekr.domain.user.domain.repository.UserRepository
import com.peekr.domain.user.infrastructure.mapper.UserMapper.toDomain
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.intLiteral
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class UserRepositoryImpl : UserRepository {
    override suspend fun findById(id: UserId): User? = suspendTransaction {
        UserEntity
            .findById(id.value)
            ?.toDomain()
    }

    override suspend fun existsUser(id: UserId): Boolean = suspendTransaction {
        Users
            .select(intLiteral(1))
            .where { Users.id eq id.value }
            .limit(1)
            .any()
    }

    override suspend fun findVisibleById(
        currentId: UserId,
        id: UserId,
    ): User? = suspendTransaction {
        Users
            .selectAll()
            .where {
                (Users.id eq id.value) and
                    isNotBlockedRelationship(myUserId = currentId.value, otherUserId = id.value)
            }.map { it.toDomain() }
            .singleOrNull()
    }

    override suspend fun findByIds(ids: List<UserId>): List<User> = suspendTransaction {
        UserEntity
            .find { Users.id inList ids.map { it.value } }
            .map { it.toDomain() }
    }

    override suspend fun findByDisplayId(id: DisplayId): User? = suspendTransaction {
        UserEntity
            .find { Users.displayId eq id.value }
            .singleOrNull()
            ?.toDomain()
    }

    override suspend fun update(
        userId: UserId,
        patch: UserPatch,
    ): Boolean = suspendTransaction {
        Users.update({ (Users.id eq userId.value) }) { row ->
            row[name] = patch.userName.value
            row[profileImageUrl] = patch.profileImageUrl
            patch.introduce.let { row[introduce] = it.value }
            patch.profileImageUrl?.let { row[profileImageUrl] = it }
        } > 0
    }

    override suspend fun updateIntroduce(
        userId: UserId,
        introduce: Introduce,
    ): Boolean = suspendTransaction {
        Users.update({ (Users.id eq userId.value) }) { row ->
            row[this.introduce] = introduce.value
        } > 0
    }
}
