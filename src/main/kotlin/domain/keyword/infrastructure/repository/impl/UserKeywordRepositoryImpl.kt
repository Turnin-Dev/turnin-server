package com.peekr.domain.keyword.infrastructure.repository.impl

import com.peekr.common.db.DatabaseFactory.dbQuery
import com.peekr.common.db.schema.Keywords
import com.peekr.common.db.schema.UserKeywordEntity
import com.peekr.common.db.schema.UserKeywords
import com.peekr.common.db.schema.Users
import com.peekr.domain.core.model.UserId
import com.peekr.domain.keyword.domain.model.KeywordId
import com.peekr.domain.keyword.domain.model.UserKeyword
import com.peekr.domain.keyword.domain.model.UserKeywordId
import com.peekr.domain.keyword.domain.model.UserKeywordPatch
import com.peekr.domain.keyword.domain.repository.UserKeywordRepository
import com.peekr.domain.keyword.infrastructure.mapper.KeywordMapper.toDomain
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update

class UserKeywordRepositoryImpl : UserKeywordRepository {
    override suspend fun getListById(userId: UserId): List<UserKeyword> = dbQuery {
        UserKeywordEntity
            .find(UserKeywords.userId eq userId.id)
            .map { it.toDomain() }
    }

    override suspend fun findByKeywordIdAndUserId(
        keywordId: KeywordId,
        userId: UserId,
    ): UserKeyword? = dbQuery {
        UserKeywordEntity
            .find(
                (UserKeywords.keywordId eq keywordId.id) and
                    (UserKeywords.userId eq userId.id),
            ).map { it.toDomain() }
            .singleOrNull()
    }

    override suspend fun save(
        keywordId: KeywordId,
        userId: UserId,
        offsetX: Float,
        offsetY: Float,
        description: String?,
    ): UserKeyword = dbQuery {
        val savedUserKeywordEntity = UserKeywordEntity.new {
            this.keywordId = EntityID(keywordId.id, Keywords)
            this.userId = EntityID(userId.id, Users)
            this.offsetX = offsetX.toDouble()
            this.offsetY = offsetY.toDouble()
            this.description = description
        }

        savedUserKeywordEntity.toDomain()
    }

    override suspend fun update(
        userKeywordId: UserKeywordId,
        patch: UserKeywordPatch,
    ): Boolean = dbQuery {
        UserKeywords.update({ UserKeywords.id eq userKeywordId.id }) {
            it[offsetX] = patch.offsetX.toDouble()
            it[offsetY] = patch.offsetY.toDouble()
            it[description] = patch.description
        } > 0
    }

    override suspend fun delete(userKeywordId: UserKeywordId): Boolean = dbQuery {
        UserKeywords.deleteWhere { id eq userKeywordId.id } > 0
    }
}
