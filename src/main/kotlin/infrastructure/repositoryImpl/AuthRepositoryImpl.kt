package com.peekr.infrastructure.repositoryImpl

import com.peekr.domain.model.entity.auth.AuthUser
import com.peekr.domain.model.value.auth.SocialLoginProvider
import com.peekr.domain.repository.auth.AuthRepository
import com.peekr.infrastructure.DatabaseFactory.dbQuery
import com.peekr.infrastructure.mapper.auth.AuthMapper.toDomain
import com.peekr.infrastructure.persistence.Users
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId

class AuthRepositoryImpl : AuthRepository {
    override suspend fun findByProviderAndProviderId(
        provider: SocialLoginProvider,
        providerId: String,
    ): AuthUser? = dbQuery {
        Users
            .select((Users.provider eq provider) and (Users.providerId eq providerId))
            .map {
                toDomain(it)
            }.singleOrNull()
    }

    override suspend fun save(authUser: AuthUser): AuthUser = dbQuery {
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
