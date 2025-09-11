package com.peekr.domain.userKeyword.infrastructure.repository.impl

import com.peekr.common.db.DatabaseFactory
import com.peekr.common.db.schema.Keywords
import com.peekr.common.db.schema.UserKeywordEntity
import com.peekr.common.db.schema.UserKeywords
import com.peekr.common.db.schema.Users
import com.peekr.domain.core.model.KeywordId
import com.peekr.domain.core.model.UserId
import com.peekr.domain.core.model.UserKeywordId
import com.peekr.domain.keyword.infrastructure.mapper.KeywordMapper.toDomain
import com.peekr.domain.userKeyword.domain.model.UserKeyword
import com.peekr.domain.userKeyword.domain.model.UserKeywordPatch
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update

class UserKeywordRepositoryImpl : UserKeywordRepository {
    override suspend fun findByUserId(userId: UserId): List<UserKeyword> = DatabaseFactory.dbQuery {
        UserKeywordEntity.Companion
            .find(UserKeywords.userId eq userId.value)
            .map { it.toDomain() }
    }

    override suspend fun findByKeywordIdAndUserId(
        keywordId: KeywordId,
        userId: UserId,
    ): UserKeyword? = DatabaseFactory.dbQuery {
        UserKeywordEntity.Companion
            .find(
                (UserKeywords.keywordId eq keywordId.value) and
                    (UserKeywords.userId eq userId.value),
            ).map { it.toDomain() }
            .singleOrNull()
    }

    override suspend fun create(
        keywordId: KeywordId,
        userId: UserId,
        offsetX: Float,
        offsetY: Float,
        description: String?,
    ): UserKeyword = DatabaseFactory.dbQuery {
        val savedUserKeywordEntity = UserKeywordEntity.Companion.new {
            this.keywordId = EntityID(keywordId.value, Keywords)
            this.userId = EntityID(userId.value, Users)
            this.offsetX = offsetX.toDouble()
            this.offsetY = offsetY.toDouble()
            this.description = description
        }

        savedUserKeywordEntity.toDomain()
    }

    override suspend fun update(
        ownerId: UserId,
        userKeywordId: UserKeywordId,
        patch: UserKeywordPatch,
    ): Boolean = DatabaseFactory.dbQuery {
        UserKeywords.update({ (UserKeywords.id eq userKeywordId.value) and (UserKeywords.userId eq ownerId.value) }) {
            it[offsetX] = patch.offsetX.toDouble()
            it[offsetY] = patch.offsetY.toDouble()
            it[description] = patch.description
        } > 0
    }

    override suspend fun delete(
        ownerId: UserId,
        userKeywordId: UserKeywordId,
    ): Boolean = DatabaseFactory.dbQuery {
        UserKeywords.deleteWhere { (id eq userKeywordId.value) and (UserKeywords.userId eq ownerId.value) } > 0
    }
}
