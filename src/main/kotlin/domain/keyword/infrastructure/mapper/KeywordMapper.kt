package com.turnin.domain.keyword.infrastructure.mapper

import com.turnin.common.db.schema.KeywordEntity
import com.turnin.common.db.schema.Keywords
import com.turnin.common.model.KeywordName
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.domain.keyword.domain.model.Keyword
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
