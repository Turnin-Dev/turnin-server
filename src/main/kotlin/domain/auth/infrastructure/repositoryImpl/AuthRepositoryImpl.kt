package com.peekr.domain.auth.infrastructure.repositoryImpl

import com.peekr.common.db.DatabaseFactory.dbQuery
import com.peekr.common.db.scheme.SocialLoginProvider
import com.peekr.common.db.scheme.UserEntity
import com.peekr.common.db.scheme.Users
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.exception.AuthException
import com.peekr.domain.auth.infrastructure.mapper.AuthMapper
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and

class AuthRepositoryImpl : AuthRepository {
    override suspend fun findAuthUserByProviderAndProviderId(
        provider: SocialLoginProvider,
        providerId: String,
    ): AuthUser? = dbQuery {
        UserEntity
            .find((Users.provider eq provider) and (Users.providerId eq providerId))
            .map {
                AuthMapper.toDomain(it.readValues)
            }.singleOrNull()
    }

    override suspend fun getUserByName(name: String): AuthUser? = dbQuery {
        UserEntity
            .find((Users.name eq name))
            .map {
                AuthMapper.toDomain(it.readValues)
            }.singleOrNull()
    }

    override suspend fun save(authUser: AuthUser): AuthUser = dbQuery {
        try {
            val savedUserEntity = UserEntity.new {
                this.provider = authUser.provider
                this.providerId = authUser.providerId
                this.name = authUser.name
                this.nickname = authUser.nickname
                this.profileImageUrl = authUser.profileImageUrl
                this.introduce = authUser.introduce
            }

            authUser.copy(id = savedUserEntity.id.value)
        } catch (e: ExposedSQLException) {
            if (e.message?.contains("Unique index") == true ||
                e.message?.contains("primary key violation") == true
            ) {
                throw AuthException.DuplicateUserException(e.message)
            }
            throw e
        }
    }
}
