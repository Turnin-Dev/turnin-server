package com.peekr.domain.keyword.infrastructure.repository.impl

import com.peekr.common.db.DatabaseFactory.dbQuery
import com.peekr.common.db.schema.KeywordEntity
import com.peekr.common.db.schema.Users
import com.peekr.domain.core.model.KeywordId
import com.peekr.domain.core.model.UserId
import com.peekr.domain.keyword.domain.model.Keyword
import com.peekr.domain.keyword.domain.repository.KeywordRepository
import com.peekr.domain.keyword.infrastructure.mapper.KeywordMapper.toDomain
import org.jetbrains.exposed.dao.id.EntityID

class KeywordRepositoryImpl : KeywordRepository {
    override suspend fun findById(id: KeywordId): Keyword? = dbQuery {
        KeywordEntity.findById(id.value)?.toDomain()
    }

    override suspend fun create(
        keyword: String,
        createdBy: UserId,
    ): Keyword = dbQuery {
        val savedKeyword = KeywordEntity.new {
            this.keyword = keyword
            this.createdBy = EntityID(createdBy.value, Users)
        }
        savedKeyword.toDomain()
    }
}
