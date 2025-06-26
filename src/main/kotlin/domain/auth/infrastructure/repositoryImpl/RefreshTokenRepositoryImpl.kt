package com.peekr.domain.auth.infrastructure.repositoryImpl

import com.peekr.common.db.DatabaseFactory
import com.peekr.common.db.scheme.RefreshTokens
import com.peekr.common.db.scheme.Users
import com.peekr.domain.auth.domain.repository.RefreshTokenRepository
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class RefreshTokenRepositoryImpl : RefreshTokenRepository {
    override suspend fun findNameByRefreshToken(token: String): String? = DatabaseFactory.dbQuery {
        RefreshTokens
            .join(Users, JoinType.INNER, RefreshTokens.user, Users.id)
            .select((RefreshTokens.refreshToken eq token))
            .singleOrNull()
            ?.get(Users.name)
    }
}
