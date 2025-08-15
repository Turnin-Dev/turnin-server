package com.peekr.domain.auth.infrastructure.repository.impl

import com.peekr.common.db.DatabaseFactory.dbQuery
import com.peekr.common.db.schema.RefreshTokens
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.db.schema.Users
import com.peekr.common.util.AppLoggerFactory
import com.peekr.common.util.AppLoggerFactory.error
import com.peekr.domain.auth.domain.repository.RefreshTokenRepository
import com.peekr.domain.auth.exception.AuthException
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.upsert

class RefreshTokenRepositoryImpl : RefreshTokenRepository {
    override suspend fun findUserIDByRefreshToken(token: String): Long? = dbQuery {
        val result = RefreshTokens
            .join(Users, JoinType.INNER, RefreshTokens.user, Users.id)
            .select(Users.id)
            .where { RefreshTokens.refreshToken eq token }
            .singleOrNull()

        result?.get(Users.id)?.value
    }

    override suspend fun save(userId: Long, token: String): Boolean = dbQuery {
        try {
            val userEntity = UserEntity.findById(userId)
            if (userEntity == null) {
                false
            } else {
                RefreshTokens.upsert {
                    it[user] = userEntity.id
                    it[refreshToken] = token
                }
                true
            }
        } catch (e: JdbcSQLIntegrityConstraintViolationException) {
            // 거의 불가능한 상황이긴 하다.
            throw AuthException.CannotSaveRefreshTokenException(e.message)
        } catch (e: Exception) {
            LOGGER.error(e)
            false
        }
    }
}

private val LOGGER = AppLoggerFactory.createLogger("RefreshTokenRepositoryImpl")
