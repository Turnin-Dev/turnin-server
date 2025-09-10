package com.peekr.domain.auth.infrastructure.repository.impl

import com.peekr.common.db.DatabaseFactory.dbQuery
import com.peekr.common.db.DatabaseUtils.eqEnum
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.db.schema.Users
import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.util.PeekrDateTime
import com.peekr.common.util.masking
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.domain.model.Register
import com.peekr.domain.auth.domain.model.RoleForAuth
import com.peekr.domain.auth.domain.model.SocialLoginProviderForAuth
import com.peekr.domain.auth.domain.model.toAuthUser
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.infrastructure.mapper.AuthMapper
import com.peekr.domain.auth.infrastructure.mapper.toRole
import com.peekr.domain.auth.infrastructure.mapper.toSocialLoginProvider
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and

class AuthRepositoryImpl : AuthRepository {
    override suspend fun findAuthUserByProviderAndProviderId(
        provider: SocialLoginProviderForAuth,
        providerId: String,
    ): AuthUser? = dbQuery {
        UserEntity
            .find(
                (Users.provider eqEnum provider.toSocialLoginProvider()) and (Users.providerId eq providerId),
            ).map {
                AuthMapper.toDomain(it.readValues)
            }.singleOrNull()
    }

    override suspend fun findUserByUserId(userId: Long): AuthUser? = dbQuery {
        UserEntity.findById(userId)?.let {
            AuthMapper.toDomain(it.readValues)
        }
    }

    override suspend fun save(register: Register): AuthUser = dbQuery {
        val role = RoleForAuth.USER
        val isActive = true
        val lastLoginAt = PeekrDateTime.now()

        val savedUserEntity = UserEntity.new {
            this.role = role.toRole()
            this.provider = register.provider.toSocialLoginProvider()
            this.providerId = register.providerId
            this.name = register.name
            this.displayId = register.displayId
            this.profileImageUrl = register.profileImageUrl
            this.introduce = register.introduce
            this.isActive = isActive
            this.lastLoginAt = lastLoginAt
        }

        register.toAuthUser(
            id = savedUserEntity.id.value,
            role = role,
            isActive = isActive,
            lastLoginAt = savedUserEntity.lastLoginAt,
        )
    }

    override suspend fun updateLastLoginAt(userId: Long) = dbQuery<Unit> {
        UserEntity.findByIdAndUpdate(userId) {
            it.lastLoginAt = PeekrDateTime.now()
        } ?: LOGGER.warn("updateLastLoginAt: user not found. userId=${userId.masking()}")
    }

    override suspend fun existsByDisplayId(displayId: String): Boolean = dbQuery {
        UserEntity
            .find((Users.displayId eq displayId))
            .limit(1)
            .empty()
            .not()
    }
}

private val LOGGER = AppLoggerFactory.createLogger("AuthRepositoryImpl")
