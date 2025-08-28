package com.peekr.domain.auth.infrastructure.repository.impl

import com.peekr.common.db.DatabaseFactory.dbQuery
import com.peekr.common.db.schema.RefreshTokens
import com.peekr.common.db.schema.UserEntity
import com.peekr.common.db.schema.Users
import com.peekr.common.util.AppLoggerFactory
import com.peekr.domain.auth.domain.repository.RefreshTokenRepository
import com.peekr.domain.auth.exception.AuthException
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.upsert

// TODO: 리프레시 토큰 평문 비교는 추후 해싱 고려 필요
// findUserIDByRefreshToken()에서 (.where { RefreshTokens.refreshToken eq token }) 부분
// RefreshTokens.refreshToken eq token은 DB에 평문 저장/비교를 전제로 합니다.
// 운영 환경에서는 리프레시 토큰을 해싱(SHA-256 등) 저장 후 비교하는 패턴을 권장합니다.
// 토큰 탈취 시 피해 최소화 및 규제/감사 대응 측면에서 유리합니다.
// 원한다면, 해싱 전략(솔트 포함)과 마이그레이션 플랜(기존 데이터 처리)까지 제안드릴 수 있습니다.
class RefreshTokenRepositoryImpl : RefreshTokenRepository {
    override suspend fun findUserIdByRefreshToken(token: String): Long? = dbQuery {
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
        } catch (e: java.sql.SQLException) {
            // 23505: ANSI/PG에서 주로 사용되는 unique_violation
            if (e.sqlState == "23505") {
                LOGGER.warn("Unique violation while upserting RefreshTokens. userId=$userId", e)
                throw AuthException.CannotSaveRefreshTokenException(e)
            } else {
                throw e
            }
        } catch (e: Exception) {
            LOGGER.error(e, "Failed to save refresh token. userId=$userId, tokenLength=${token.length}")
            false
        }
    }
}

private val LOGGER = AppLoggerFactory.createLogger("RefreshTokenRepositoryImpl")
