package com.peekr.domain.auth.infrastructure.repositoryImpl

import com.peekr.common.db.DatabaseFactory
import com.peekr.common.db.scheme.RefreshTokens
import com.peekr.common.db.scheme.Users
import com.peekr.domain.auth.domain.repository.RefreshTokenRepository
import org.jetbrains.exposed.sql.JoinType

class RefreshTokenRepositoryImpl : RefreshTokenRepository {
    override suspend fun findNameByRefreshToken(token: String): String? = DatabaseFactory.dbQuery {
        val result = RefreshTokens
            .join(Users, JoinType.INNER, RefreshTokens.user, Users.id)
            .select(Users.name)
            .where { RefreshTokens.refreshToken eq token }
            .singleOrNull()

        if (result == null) null

        result?.get(Users.name)
    }
}
