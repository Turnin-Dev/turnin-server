package com.turnin.domain.auth.infrastructure.repository.impl

import com.turnin.common.db.extension.filterActiveUser
import com.turnin.common.db.schema.RefreshTokens
import com.turnin.common.db.schema.Users
import com.turnin.common.db.suspendTransaction
import com.turnin.common.model.id.UserId
import com.turnin.domain.auth.domain.repository.RefreshTokenRepository
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.upsert

// TODO: 리프레시 토큰 평문 비교는 추후 해싱 고려 필요
// findUserIDByRefreshToken()에서 (.where { RefreshTokens.refreshToken eq token }) 부분
// RefreshTokens.refreshToken eq token은 DB에 평문 저장/비교를 전제로 합니다.
// 운영 환경에서는 리프레시 토큰을 해싱(SHA-256 등) 저장 후 비교하는 패턴을 권장합니다.
// 토큰 탈취 시 피해 최소화 및 규제/감사 대응 측면에서 유리합니다.
// 원한다면, 해싱 전략(솔트 포함)과 마이그레이션 플랜(기존 데이터 처리)까지 제안드릴 수 있습니다.
class RefreshTokenRepositoryImpl : RefreshTokenRepository {
    override suspend fun findUserIdByRefreshToken(token: String): UserId? = suspendTransaction {
        val result = RefreshTokens
            .join(Users, JoinType.INNER, RefreshTokens.user, Users.id)
            .select(Users.id)
            .where { RefreshTokens.refreshToken eq token }
            .filterActiveUser()
            .singleOrNull()

        result?.get(Users.id)?.let {
            UserId(it.value)
        }
    }

    override suspend fun save(userId: UserId, token: String): Boolean = suspendTransaction {
        RefreshTokens
            .upsert {
                it[user] = EntityID(userId.value, Users)
                it[refreshToken] = token
            }.resultedValues
            ?.isNotEmpty() == true
    }

    override suspend fun delete(userId: UserId): Unit = suspendTransaction {
        RefreshTokens.deleteWhere { RefreshTokens.user eq userId.value }
    }
}
