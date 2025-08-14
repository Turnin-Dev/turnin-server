package com.peekr.domain.auth.infrastructure.repository.impl

import com.peekr.common.db.DatabaseFactory.dbQuery
import com.peekr.common.db.schema.RefreshTokens
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.db.schema.Users
import com.peekr.domain.auth.domain.repository.RefreshTokenRepository
import com.peekr.domain.auth.exception.AuthException
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.upsert

class RefreshTokenRepositoryImpl : RefreshTokenRepository {
    override suspend fun findDisplayIdByRefreshToken(token: String): String? = dbQuery {
        val result = RefreshTokens
            .join(Users, JoinType.INNER, RefreshTokens.user, Users.id)
            .select(Users.displayId)
            .where { RefreshTokens.refreshToken eq token }
            .singleOrNull()

        result?.get(Users.displayId)
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
            false
        }
    }
}
