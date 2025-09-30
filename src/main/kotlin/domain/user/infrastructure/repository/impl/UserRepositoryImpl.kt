package com.peekr.domain.user.infrastructure.repository.impl

import com.peekr.common.db.DatabaseFactory.dbQuery
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.db.schema.Users
import com.peekr.common.model.UserId
import com.peekr.domain.user.domain.model.User
import com.peekr.domain.user.domain.model.UserPatch
import com.peekr.domain.user.domain.repository.UserRepository
import com.peekr.domain.user.infrastructure.mapper.UserMapper
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.update

class UserRepositoryImpl : UserRepository {
    override suspend fun findById(id: UserId): User? = dbQuery {
        UserEntity.findById(id.value)?.let { entity ->
            UserMapper.toDomain(entity)
        }
    }

    override suspend fun update(
        userId: UserId,
        patch: UserPatch,
    ): Boolean = dbQuery {
        Users.update({ (Users.id eq userId.value) }) {
            it[displayId] = patch.displayId.value
            it[name] = patch.name.value
            it[introduce] = patch.introduce
            it[profileImageUrl] = patch.profileImageUrl
        } > 0
    }
}
