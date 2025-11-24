package com.peekr.domain.user.infrastructure.repository.impl

import com.peekr.common.db.schema.UserEntity
import com.peekr.common.db.schema.Users
import com.peekr.common.db.suspendTransaction
import com.peekr.common.model.Introduce
import com.peekr.common.model.id.UserId
import com.peekr.domain.user.domain.model.User
import com.peekr.domain.user.domain.model.UserPatch
import com.peekr.domain.user.domain.repository.UserRepository
import com.peekr.domain.user.infrastructure.mapper.UserMapper
import org.jetbrains.exposed.sql.update

class UserRepositoryImpl : UserRepository {
    override suspend fun findById(id: UserId): User? = suspendTransaction {
        UserEntity.findById(id.value)?.let { entity ->
            UserMapper.toDomain(entity)
        }
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
        introduce: Introduce,
    ): Boolean = suspendTransaction {
        Users.update({ (Users.id eq userId.value) }) { row ->
            row[this.introduce] = introduce.value
        } > 0
    }
}
