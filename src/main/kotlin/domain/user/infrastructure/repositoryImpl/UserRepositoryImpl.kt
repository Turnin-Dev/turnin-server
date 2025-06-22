package com.peekr.domain.user.infrastructure.repositoryImpl

import com.peekr.common.db.DatabaseFactory.dbQuery
import com.peekr.common.db.scheme.UserEntity
import com.peekr.domain.user.domain.repository.UserRepository

class UserRepositoryImpl : UserRepository {
    override suspend fun getUserById(id: Long): UserEntity? = dbQuery {
        UserEntity.findById(id)
    }
}
