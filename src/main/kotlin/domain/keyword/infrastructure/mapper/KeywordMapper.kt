package com.peekr.domain.keyword.infrastructure.mapper

import com.peekr.common.db.schema.KeywordEntity
import com.peekr.common.db.schema.Keywords
import com.peekr.common.db.schema.UserKeywordEntity
import com.peekr.domain.core.model.KeywordId
import com.peekr.domain.core.model.UserId
import com.peekr.domain.core.model.UserKeywordId
import com.peekr.domain.keyword.domain.model.Keyword
import com.peekr.domain.userKeyword.domain.model.UserKeyword
import org.jetbrains.exposed.sql.ResultRow

object KeywordMapper {
    fun toDomain(row: ResultRow): Keyword =
        Keyword(
            id = KeywordId(row[Keywords.id].value),
            keyword = row[Keywords.keyword],
            createdBy = UserId(row[Keywords.createdBy].value),
            createdAt = row[Keywords.createdAt].toEpochSecond(),
            updatedAt = row[Keywords.updatedAt].toEpochSecond(),
        )

    fun KeywordEntity.toDomain(): Keyword =
        Keyword(
            id = KeywordId(this.id.value),
            keyword = this.keyword,
            createdBy = UserId(this.createdBy.value),
            createdAt = this.createdAt.toEpochSecond(),
            updatedAt = this.updatedAt.toEpochSecond(),
        )

    fun UserKeywordEntity.toDomain(): UserKeyword =
        UserKeyword(
            id = UserKeywordId(this.id.value),
            keywordId = KeywordId(this.keywordId.value),
            userId = UserId(this.userId.value),
            offsetX = this.offsetX.toFloat(),
            offsetY = this.offsetY.toFloat(),
            description = this.description,
            createdAt = this.createdAt.toEpochSecond(),
            updatedAt = this.updatedAt.toEpochSecond(),
        )
}
