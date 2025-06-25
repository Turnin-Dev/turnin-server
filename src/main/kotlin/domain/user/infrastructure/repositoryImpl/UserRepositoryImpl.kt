package com.peekr.domain.user.infrastructure.repositoryImpl

import com.peekr.common.db.DatabaseFactory.dbQuery
import com.peekr.common.db.scheme.UserEntity
import com.peekr.domain.user.domain.model.User
import com.peekr.domain.user.domain.repository.UserRepository
import com.peekr.domain.user.infrastructure.mapper.UserMapper

class UserRepositoryImpl : UserRepository {
    override suspend fun getUserById(id: Long): User? = dbQuery {
        UserEntity.findById(id)?.let { entity ->
            UserMapper.toDomain(entity)
        }
    }
}
