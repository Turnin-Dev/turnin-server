package com.peekr.domain.auth.infrastructure.repositoryImpl

import com.peekr.common.infrastructure.DatabaseFactory
import com.peekr.domain.auth.domain.model.entity.AuthUser
import com.peekr.domain.auth.domain.model.value.SocialLoginProvider
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.infrastructure.mapper.AuthMapper
import com.peekr.domain.auth.infrastructure.persistence.Users
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId

class AuthRepositoryImpl : AuthRepository {
    override suspend fun findByProviderAndProviderId(
        provider: SocialLoginProvider,
        providerId: String,
    ): AuthUser? = DatabaseFactory.dbQuery {
        Users
            .select((Users.provider eq provider) and (Users.providerId eq providerId))
            .map {
                AuthMapper.toDomain(it)
            }.singleOrNull()
    }

    override suspend fun save(authUser: AuthUser): AuthUser = DatabaseFactory.dbQuery {
        val id = Users
            .insertAndGetId {
                it[provider] = authUser.provider
                it[providerId] = authUser.providerId
                it[name] = authUser.name
                it[nickname] = authUser.nickname
                it[profileImageUrl] = authUser.profileImageUrl
                it[introduce] = authUser.introduce
            }.value

        authUser.copy(id = id)
    }
}
