package com.peekr.domain.auth.infrastructure.repository.impl

import com.peekr.common.db.DatabaseException
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

    override suspend fun save(register: Register): AuthUser = dbQuery {
        try {
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
        } catch (e: Exception) {
            throw processSQLException(e)
        }
    }

    override suspend fun updateLastLoginAt(userId: Long) = dbQuery<Unit> {
        UserEntity.findByIdAndUpdate(userId) {
            it.lastLoginAt = PeekrDateTime.now()
        } ?: LOGGER.warn("updateLastLoginAt: user not found. userId=${userId.masking()}")
    }

    // 커스텀 예외를 던지거나 발생한 예외를 그대로 전파한다.
    private fun processSQLException(e: Exception): Throwable {
        val sqlState: String? = when (e) {
            is ExposedSQLException -> e.sqlState
            is SQLException -> e.sqlState
            is DatabaseException.DBQueryException -> e.throwable?.sqlState
            else -> null
        }
        val causeMsg = when (e) {
            is DatabaseException.DBQueryException -> e.throwable?.message
            is ExposedSQLException -> e.cause?.message
            else -> null
        }
        val isDuplicateByMsg = sequenceOf(e.message, causeMsg).any {
            it?.contains("already exists", ignoreCase = true) == true ||
                it?.contains("primary key violation", ignoreCase = true) == true
        }
        return if (sqlState == "23505" || isDuplicateByMsg) {
            LOGGER.debug("Duplicate user detected while saving authUser.", e)
            AuthException.DuplicateUserException(e)
        } else {
            e
        }
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
