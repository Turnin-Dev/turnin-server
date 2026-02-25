package com.peekr.domain.keyword.infrastructure.mapper

import com.peekr.common.db.schema.KeywordEntity
import com.peekr.common.db.schema.Keywords
import com.peekr.common.model.KeywordName
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.domain.keyword.domain.model.Keyword
import org.jetbrains.exposed.sql.ResultRow

object KeywordMapper {
    fun ResultRow.toDomain(): Keyword =
        Keyword(
            id = KeywordId(this[Keywords.id].value),
            name = KeywordName(this[Keywords.keyword]),
            embedding = this[Keywords.embedding],
            createdBy = this[Keywords.createdBy]?.let { UserId(it.value) },
            createdAt = this[Keywords.createdAt].toEpochSecond(),
            updatedAt = this[Keywords.updatedAt].toEpochSecond(),
        )

    fun KeywordEntity.toDomain(): Keyword =
        Keyword(
            id = KeywordId(this.id.value),
            name = KeywordName(this.keyword),
            embedding = this.embedding,
            createdBy = this.createdBy?.let { UserId(it.value) },
            createdAt = this.createdAt.toEpochSecond(),
            updatedAt = this.updatedAt.toEpochSecond(),
        )
}
