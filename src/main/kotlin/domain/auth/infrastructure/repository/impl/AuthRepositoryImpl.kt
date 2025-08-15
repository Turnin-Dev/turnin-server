package com.peekr.domain.auth.infrastructure.repository.impl

import com.peekr.common.db.DatabaseFactory.dbQuery
import com.peekr.common.db.DatabaseUtils.eqEnum
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.db.schema.Users
import com.peekr.common.util.AppLoggerFactory
import com.peekr.domain.auth.domain.model.AuthUser
import com.peekr.domain.auth.domain.model.SocialLoginProviderForAuth
import com.peekr.domain.auth.domain.repository.AuthRepository
import com.peekr.domain.auth.exception.AuthException
import com.peekr.domain.auth.infrastructure.mapper.AuthMapper
import com.peekr.domain.auth.infrastructure.mapper.toRole
import com.peekr.domain.auth.infrastructure.mapper.toSocialLoginProvider
import java.sql.SQLException
import org.jetbrains.exposed.exceptions.ExposedSQLException
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

    override suspend fun save(authUser: AuthUser): AuthUser = dbQuery {
        try {
            val savedUserEntity = UserEntity.new {
                this.role = authUser.role.toRole()
                this.provider = authUser.provider.toSocialLoginProvider()
                this.providerId = authUser.providerId
                this.name = authUser.name
                this.displayId = authUser.displayId
                this.profileImageUrl = authUser.profileImageUrl
                this.introduce = authUser.introduce
                this.isActive = authUser.isActive
                this.lastLoginAt = authUser.lastLoginAt
            }

            authUser.copy(id = savedUserEntity.id.value)
        } catch (e: Exception) {
            processSQLException(e)
            throw e
        }
    }

    private fun processSQLException(e: Throwable) {
        if (e is ExposedSQLException || e is SQLException) {
            if (e.message?.contains("Unique index") == true ||
                e.message?.contains("primary key violation") == true ||
                e.message?.contains("already exists") == true
            ) {
                LOGGER.debug("Duplicate user detected while saving authUser.", e)
                throw AuthException.DuplicateUserException(e.message)
            }
        }
    }
}

private val LOGGER = AppLoggerFactory.createLogger("AuthRepositoryImpl")
