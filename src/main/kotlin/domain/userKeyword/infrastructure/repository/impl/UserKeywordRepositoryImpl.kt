package com.turnin.domain.userKeyword.infrastructure.repository.impl

import com.turnin.common.db.DatabaseUtils.isNotBlockedRelationship
import com.turnin.common.db.extension.filterActiveUser
import com.turnin.common.db.extension.filterActiveUserKeyword
import com.turnin.common.db.schema.Keywords
import com.turnin.common.db.schema.UserKeywordEntity
import com.turnin.common.db.schema.UserKeywords
import com.turnin.common.db.schema.Users
import com.turnin.common.db.suspendTransaction
import com.turnin.common.db.updateWithTimestamp
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.userKeyword.domain.model.Description
import com.turnin.domain.userKeyword.domain.model.UserKeyword
import com.turnin.domain.userKeyword.domain.model.UserKeywordDetail
import com.turnin.domain.userKeyword.domain.model.UserKeywordPatch
import com.turnin.domain.userKeyword.domain.repository.UserKeywordRepository
import com.turnin.domain.userKeyword.infrastructure.mapper.UserKeywordMapper.toDetail
import com.turnin.domain.userKeyword.infrastructure.mapper.UserKeywordMapper.toDomain
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.selectAll

class UserKeywordRepositoryImpl : UserKeywordRepository {
    override suspend fun findById(userKeywordId: UserKeywordId): UserKeyword? = suspendTransaction {
        UserKeywords
            .selectAll()
            .where { UserKeywords.id eq userKeywordId.value }
            .filterActiveUserKeyword()
            .map { it.toDomain() }
            .singleOrNull()
    }

    override suspend fun findListByUserId(userId: UserId): List<UserKeyword> = suspendTransaction {
        UserKeywords
            .select(
                UserKeywords.id,
                UserKeywords.userId,
                UserKeywords.keywordId,
                UserKeywords.description,
                UserKeywords.createdAt,
                UserKeywords.updatedAt,
            ).where(UserKeywords.userId eq userId.value)
            .filterActiveUserKeyword()
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
                UserKeywords.description,
                UserKeywords.createdAt,
                UserKeywords.updatedAt,
            ).where(
                (UserKeywords.keywordId eq keywordId.value) and
                    (UserKeywords.userId eq userId.value),
            ).filterActiveUserKeyword()
            .map { it.toDomain() }
            .singleOrNull()
    }

    override suspend fun getDetailById(
        currentUserId: UserId,
        userKeywordId: UserKeywordId,
    ): UserKeywordDetail? = suspendTransaction {
        val joinQuery = UserKeywords
            .innerJoin(
                otherTable = Keywords,
                onColumn = { UserKeywords.keywordId },
                otherColumn = { Keywords.id },
            ).innerJoin(
                otherTable = Users,
                onColumn = { UserKeywords.userId },
                otherColumn = { Users.id },
            )

        joinQuery
            .select(
                UserKeywords.id,
                UserKeywords.keywordId,
                UserKeywords.description,
                UserKeywords.createdAt,
                UserKeywords.updatedAt,
                Keywords.keyword,
                Users.id,
                Users.name,
                Users.profileImageUrl,
            ).where {
                (UserKeywords.id eq userKeywordId.value) and
                    isNotBlockedRelationship(myUserId = currentUserId.value, Users.id)
            }.filterActiveUser()
            .filterActiveUserKeyword()
            .map { it.toDetail() }
            .singleOrNull()
    }

    override suspend fun getDetailsByUserId(
        currentUserId: UserId,
        userId: UserId,
    ): List<UserKeywordDetail> = suspendTransaction {
        val joinQuery = UserKeywords
            .innerJoin(
                otherTable = Keywords,
                onColumn = { UserKeywords.keywordId },
                otherColumn = { Keywords.id },
            ).innerJoin(
                otherTable = Users,
                onColumn = { UserKeywords.userId },
                otherColumn = { Users.id },
            )

        joinQuery
            .select(
                UserKeywords.id,
                UserKeywords.keywordId,
                UserKeywords.description,
                UserKeywords.createdAt,
                UserKeywords.updatedAt,
                Keywords.keyword,
                Users.id,
                Users.name,
                Users.profileImageUrl,
            ).where {
                (UserKeywords.userId eq userId.value) and
                    isNotBlockedRelationship(myUserId = currentUserId.value, Users.id)
            }.filterActiveUser()
            .filterActiveUserKeyword()
            .map { it.toDetail() }
    }

    override suspend fun findDescriptionById(
        ownerId: UserId,
        userKeywordId: UserKeywordId,
    ): Description? = suspendTransaction {
        UserKeywords
            .select(UserKeywords.description)
            .where((UserKeywords.id eq userKeywordId.value) and (UserKeywords.userId eq ownerId.value))
            .filterActiveUserKeyword()
            .map {
                it[UserKeywords.description]?.let {
                    Description(it)
                }
            }.singleOrNull()
    }

    override suspend fun countByUserId(userId: UserId): Long = suspendTransaction {
        UserKeywords
            .selectAll()
            .where { UserKeywords.userId eq userId.value }
            .filterActiveUserKeyword()
            .count()
    }

    override suspend fun create(
        keywordId: KeywordId,
        userId: UserId,
        description: Description,
    ): UserKeyword = suspendTransaction {
        val savedUserKeywordEntity = UserKeywordEntity.new {
            this.keywordId = EntityID(keywordId.value, Keywords)
            this.userId = EntityID(userId.value, Users)
            this.description = description.value
        }

        savedUserKeywordEntity.toDomain()
    }

    override suspend fun update(
        ownerId: UserId,
        patch: UserKeywordPatch,
    ): Boolean = suspendTransaction {
        UserKeywords.updateWithTimestamp({
            (UserKeywords.id eq patch.userKeywordId.value) and
                (UserKeywords.userId eq ownerId.value)
        }) {
            it[keywordId] = patch.keywordId.value
            it[description] = patch.description.value
        } > 0
    }

    override suspend fun delete(
        ownerId: UserId,
        userKeywordId: UserKeywordId,
    ): Boolean = suspendTransaction {
        UserKeywords.deleteWhere {
            (UserKeywords.id eq userKeywordId.value) and (UserKeywords.userId eq ownerId.value)
        } > 0
    }

    override suspend fun deactivate(
        ownerId: UserId,
        userKeywordId: UserKeywordId,
    ): Boolean = suspendTransaction {
        UserKeywords.updateWithTimestamp({
            (UserKeywords.id eq userKeywordId.value) and (UserKeywords.userId eq ownerId.value)
        }) {
            it[isActive] = false
        }
    } > 0

    override suspend fun deactivateAll(userId: UserId): Unit = suspendTransaction {
        UserKeywords.updateWithTimestamp({ UserKeywords.userId eq userId.value }) {
            it[isActive] = false
        }
    }
}
