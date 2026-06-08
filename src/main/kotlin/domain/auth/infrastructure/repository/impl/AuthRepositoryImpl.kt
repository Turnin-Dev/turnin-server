package com.turnin.domain.auth.infrastructure.repository.impl

import com.turnin.common.db.DatabaseUtils.eqEnum
import com.turnin.common.db.extension.filterActiveUser
import com.turnin.common.db.schema.UserEntity
import com.turnin.common.db.schema.Users
import com.turnin.common.db.suspendTransaction
import com.turnin.common.model.Role
import com.turnin.common.model.SocialLoginProvider
import com.turnin.common.model.id.DisplayId
import com.turnin.common.model.id.UserId
import com.turnin.common.util.TurninDateTime
import com.turnin.common.util.log.AppLoggerFactory
import com.turnin.common.util.masking
import com.turnin.domain.auth.domain.model.AuthUser
import com.turnin.domain.auth.domain.model.Register
import com.turnin.domain.auth.domain.model.toAuthUser
import com.turnin.domain.auth.domain.repository.AuthRepository
import com.turnin.domain.auth.infrastructure.mapper.AuthMapper
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class AuthRepositoryImpl : AuthRepository {
    override suspend fun findAuthUserByProviderAndProviderId(
        provider: SocialLoginProvider,
        providerId: String,
    ): AuthUser? = suspendTransaction {
        Users
            .selectAll()
            .where {
                (Users.provider eqEnum provider) and (Users.providerId eq providerId)
            }.filterActiveUser()
            .map { AuthMapper.toDomain(it) }
            .singleOrNull()
    }

    override suspend fun findUserByUserId(userId: UserId): AuthUser? = suspendTransaction {
        Users
            .selectAll()
            .where { Users.id eq userId.value }
            .filterActiveUser()
            .map { AuthMapper.toDomain(it) }
            .singleOrNull()
    }

    override suspend fun existsByDisplayId(displayId: DisplayId): Boolean = suspendTransaction {
        UserEntity
            .find((Users.displayId eq displayId.value))
            .limit(1)
            .empty()
            .not()
    }

    override suspend fun save(
        register: Register,
        role: Role,
    ): AuthUser = suspendTransaction {
        val isActive = true
        val lastLoginAt = TurninDateTime.now()

        val savedUserEntity = UserEntity.new {
            this.role = role
            this.provider = register.provider
            this.providerId = register.providerId
            this.name = register.userName.value
            this.displayId = register.displayId.value
            this.profileImageUrl = register.profileImageUrl
            this.introduce = register.introduce.value
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

    override suspend fun updateLastLoginAt(userId: UserId) = suspendTransaction<Unit> {
        UserEntity.findByIdAndUpdate(userId.value) {
            it.lastLoginAt = TurninDateTime.now()
        } ?: LOGGER.warn("updateLastLoginAt: user not found. userId=${userId.value.masking()}")
    }
}

private val LOGGER = AppLoggerFactory.createLogger("AuthRepositoryImpl")
