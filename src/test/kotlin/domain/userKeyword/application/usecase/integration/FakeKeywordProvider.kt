package com.turnin.domain.userKeyword.application.usecase.integration

import com.turnin.common.db.schema.KeywordEntity
import com.turnin.common.db.schema.Keywords
import com.turnin.common.db.schema.Users
import com.turnin.common.db.suspendTransaction
import com.turnin.common.model.KeywordName
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.domain.userKeyword.domain.model.ExternalKeyword
import com.turnin.domain.userKeyword.domain.provider.KeywordProvider
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.selectAll

class FakeKeywordProvider : KeywordProvider {
    override suspend fun findById(keywordId: KeywordId): ExternalKeyword? = suspendTransaction {
        Keywords
            .selectAll()
            .where { Keywords.id eq keywordId.value }
            .map { eKeyword ->
                ExternalKeyword(
                    id = KeywordId(eKeyword[Keywords.id].value),
                    name = KeywordName(eKeyword[Keywords.keyword]),
                    createdBy = eKeyword[Keywords.createdBy]?.let { UserId(it.value) },
                    createdAt = eKeyword[Keywords.createdAt].toEpochSecond(),
                    updatedAt = eKeyword[Keywords.updatedAt].toEpochSecond(),
                )
            }.singleOrNull()
    }

    override suspend fun findByIds(keywordIds: List<KeywordId>): List<ExternalKeyword> = suspendTransaction {
        Keywords
            .selectAll()
            .where { Keywords.id inList keywordIds.map { it.value } }
            .map { eKeyword ->
                ExternalKeyword(
                    id = KeywordId(eKeyword[Keywords.id].value),
                    name = KeywordName(eKeyword[Keywords.keyword]),
                    createdBy = eKeyword[Keywords.createdBy]?.let { UserId(it.value) },
                    createdAt = eKeyword[Keywords.createdAt].toEpochSecond(),
                    updatedAt = eKeyword[Keywords.updatedAt].toEpochSecond(),
                )
            }
    }

    override suspend fun findByName(keywordName: String): ExternalKeyword? = suspendTransaction {
        Keywords
            .selectAll()
            .where { Keywords.keyword eq keywordName }
            .map { eKeyword ->
                ExternalKeyword(
                    id = KeywordId(eKeyword[Keywords.id].value),
                    name = KeywordName(eKeyword[Keywords.keyword]),
                    createdBy = eKeyword[Keywords.createdBy]?.let { UserId(it.value) },
                    createdAt = eKeyword[Keywords.createdAt].toEpochSecond(),
                    updatedAt = eKeyword[Keywords.updatedAt].toEpochSecond(),
                )
            }.singleOrNull()
    }

    override suspend fun create(
        keywordName: String,
        createdBy: UserId,
    ): ExternalKeyword = suspendTransaction {
        val keyword = KeywordEntity.new {
            this.keyword = keywordName
            this.embedding = "embedding"
            this.createdBy = EntityID(createdBy.value, Users)
        }
        ExternalKeyword(
            id = KeywordId(keyword.id.value),
            name = KeywordName(keyword.keyword),
            createdBy = createdBy,
            createdAt = keyword.createdAt.toEpochSecond(),
            updatedAt = keyword.updatedAt.toEpochSecond(),
        )
    }
}
