package com.peekr.domain.keyword.infrastructure.mapper

import com.peekr.common.db.schema.KeywordEntity
import com.peekr.common.db.schema.Keywords
import com.peekr.common.model.KeywordId
import com.peekr.common.model.KeywordName
import com.peekr.common.model.UserId
import com.peekr.domain.keyword.domain.model.Keyword
import org.jetbrains.exposed.sql.ResultRow

object KeywordMapper {
    fun toDomain(row: ResultRow): Keyword =
        Keyword(
            id = KeywordId(row[Keywords.id].value),
            name = KeywordName(row[Keywords.keyword]),
            createdBy = UserId(row[Keywords.createdBy].value),
            createdAt = row[Keywords.createdAt].toEpochSecond(),
            updatedAt = row[Keywords.updatedAt].toEpochSecond(),
        )

    fun KeywordEntity.toDomain(): Keyword =
        Keyword(
            id = KeywordId(this.id.value),
            name = KeywordName(this.keyword),
            createdBy = UserId(this.createdBy.value),
            createdAt = this.createdAt.toEpochSecond(),
            updatedAt = this.updatedAt.toEpochSecond(),
        )
}
