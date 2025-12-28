package com.peekr.domain.keyword.infrastructure.repository.impl

import com.peekr.common.db.schema.KeywordEntity
import com.peekr.common.db.schema.Keywords
import com.peekr.common.db.schema.Users
import com.peekr.common.db.suspendTransaction
import com.peekr.common.model.KeywordName
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.domain.keyword.domain.model.Keyword
import com.peekr.domain.keyword.domain.repository.KeywordRepository
import com.peekr.domain.keyword.infrastructure.mapper.KeywordMapper.toDomain
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

class KeywordRepositoryImpl : KeywordRepository {
    override suspend fun findById(id: KeywordId): Keyword? = suspendTransaction {
        KeywordEntity.findById(id.value)?.toDomain()
    }

    override suspend fun findByIds(ids: List<KeywordId>): List<Keyword> = suspendTransaction {
        if (ids.isEmpty()) return@suspendTransaction emptyList()
        KeywordEntity
            .find { Keywords.id inList ids.map { it.value } }
            .map { it.toDomain() }
    }

    override suspend fun findByName(keywordName: KeywordName): Keyword? = suspendTransaction {
        KeywordEntity
            .find(Keywords.keyword eq keywordName.value)
            .firstOrNull()
            ?.toDomain()
    }

    override suspend fun create(
        keywordName: KeywordName,
        createdBy: UserId,
    ): Keyword = suspendTransaction {
        val savedKeyword = KeywordEntity.new {
            this.keyword = keywordName.value
            this.createdBy = EntityID(createdBy.value, Users)
        }
        savedKeyword.toDomain()
    }
}
