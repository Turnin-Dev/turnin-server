package com.peekr.domain.auth.infrastructure.repositoryImpl

import com.peekr.common.plugin.DatabaseFactory
import com.peekr.domain.auth.domain.model.entity.AuthUser
import com.peekr.domain.auth.domain.model.value.SocialLoginProvider
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.infrastructure.mapper.AuthMapper
import com.peekr.domain.auth.infrastructure.persistence.User
import com.peekr.domain.auth.infrastructure.persistence.Users
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and

class AuthRepositoryImpl : AuthRepository {
    override suspend fun findByProviderAndProviderId(
        provider: SocialLoginProvider,
        providerId: String,
    ): AuthUser? = DatabaseFactory.dbQuery {
        User
            .find((Users.provider eq provider) and (Users.providerId eq providerId))
            .map {
                AuthMapper.toDomain(it.readValues)
            }.singleOrNull()
    }

    override suspend fun save(authUser: AuthUser): AuthUser = DatabaseFactory.dbQuery {
        val savedUser = User.new {
            this.provider = authUser.provider
            this.providerId = authUser.providerId
            this.name = authUser.name
            this.nickname = authUser.nickname
            this.profileImageUrl = authUser.profileImageUrl
            this.introduce = authUser.introduce
        }

        authUser.copy(id = savedUser.id.value)
    }
}
