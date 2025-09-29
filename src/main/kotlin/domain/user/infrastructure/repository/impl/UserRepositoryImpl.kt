package com.peekr.domain.user.infrastructure.repository.impl

import com.peekr.common.db.DatabaseFactory.dbQuery
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.model.UserId
import com.peekr.domain.user.domain.model.User
import com.peekr.domain.user.domain.repository.UserRepository
import com.peekr.domain.user.infrastructure.mapper.UserMapper

class UserRepositoryImpl : UserRepository {
    override suspend fun getUserById(id: UserId): User? = dbQuery {
        UserEntity.findById(id.value)?.let { entity ->
            UserMapper.toDomain(entity)
        }
    }
}
