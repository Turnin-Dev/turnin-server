package com.peekr.domain.userKeyword.infrastructure.repository.impl

import com.peekr.common.db.schema.Keywords
import com.peekr.common.db.schema.UserKeywordEntity
import com.peekr.common.db.schema.UserKeywords
import com.peekr.common.db.schema.Users
import com.peekr.common.db.suspendTransaction
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.userKeyword.domain.model.Description
import com.peekr.domain.userKeyword.domain.model.Offset
import com.peekr.domain.userKeyword.domain.model.UserKeyword
import com.peekr.domain.userKeyword.domain.repository.UserKeywordRepository
import com.peekr.domain.userKeyword.infrastructure.mapper.UserKeywordMapper.toDomain
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update

class UserKeywordRepositoryImpl : UserKeywordRepository {
    override suspend fun findByUserId(userId: UserId): List<UserKeyword> = suspendTransaction {
        UserKeywords
            .select(
                UserKeywords.id,
                UserKeywords.userId,
                UserKeywords.keywordId,
                UserKeywords.offsetX,
                UserKeywords.offsetY,
                UserKeywords.createdAt,
                UserKeywords.updatedAt,
            ).where(UserKeywords.userId eq userId.value)
            .map { row -> row.toDomain() }
    }

    override suspend fun findByKeywordIdAndUserId(
        keywordId: KeywordId,
        userId: UserId,
    ): UserKeyword? = suspendTransaction {
        UserKeywords
            .select(
                UserKeywords.id,
                UserKeywords.userId,
                UserKeywords.keywordId,
                UserKeywords.offsetX,
                UserKeywords.offsetY,
                UserKeywords.createdAt,
                UserKeywords.updatedAt,
            ).where(
                (UserKeywords.keywordId eq keywordId.value) and
                    (UserKeywords.userId eq userId.value),
            ).map { it.toDomain() }
            .singleOrNull()
    }

    override suspend fun findDescriptionById(
        ownerId: UserId,
        userKeywordId: UserKeywordId,
    ): Description? = suspendTransaction {
        UserKeywords
            .select(UserKeywords.description)
            .where((UserKeywords.id eq userKeywordId.value) and (UserKeywords.userId eq ownerId.value))
            .map {
                it[UserKeywords.description]?.let {
                    Description(it)
                }
            }.singleOrNull()
    }

    override suspend fun create(
        keywordId: KeywordId,
        userId: UserId,
        offset: Offset,
        description: Description,
    ): UserKeyword = suspendTransaction {
        val savedUserKeywordEntity = UserKeywordEntity.new {
            this.keywordId = EntityID(keywordId.value, Keywords)
            this.userId = EntityID(userId.value, Users)
            this.offsetX = offset.x.toDouble()
            this.offsetY = offset.y.toDouble()
            this.description = description.value
        }

        savedUserKeywordEntity.toDomain()
    }

    override suspend fun updateOffset(
        ownerId: UserId,
        userKeywordId: UserKeywordId,
        patch: Offset,
    ): Boolean = suspendTransaction {
        UserKeywords.update({ (UserKeywords.id eq userKeywordId.value) and (UserKeywords.userId eq ownerId.value) }) {
            it[offsetX] = patch.x.toDouble()
            it[offsetY] = patch.y.toDouble()
        } > 0
    }

    override suspend fun updateDescription(
        ownerId: UserId,
        userKeywordId: UserKeywordId,
        patch: Description,
    ): Boolean = suspendTransaction {
        UserKeywords.update({ (UserKeywords.id eq userKeywordId.value) and (UserKeywords.userId eq ownerId.value) }) {
            it[description] = patch.value
        } > 0
    }

    override suspend fun delete(
        ownerId: UserId,
        userKeywordId: UserKeywordId,
    ): Boolean = suspendTransaction {
        UserKeywords.deleteWhere { (id eq userKeywordId.value) and (UserKeywords.userId eq ownerId.value) } > 0
    }
}
