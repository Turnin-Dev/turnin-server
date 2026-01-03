package com.peekr.domain.userKeyword.infrastructure.mapper

import com.peekr.common.db.schema.UserKeywordEntity
import com.peekr.common.db.schema.UserKeywords
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.userKeyword.domain.model.Description
import com.peekr.domain.userKeyword.domain.model.UserKeyword
import org.jetbrains.exposed.sql.Expression
import org.jetbrains.exposed.sql.ResultRow

internal object UserKeywordMapper {
    fun ResultRow.toDomain(descriptionAlias: Expression<String>? = null): UserKeyword =
        UserKeyword(
            id = UserKeywordId(this[UserKeywords.id].value),
            keywordId = KeywordId(this[UserKeywords.keywordId].value),
            userId = UserId(this[UserKeywords.userId].value),
            description = Description(this[descriptionAlias ?: UserKeywords.description]),
            createdAt = this[UserKeywords.createdAt].toEpochSecond(),
            updatedAt = this[UserKeywords.updatedAt].toEpochSecond(),
        )

    fun UserKeywordEntity.toDomain(): UserKeyword =
        UserKeyword(
            id = UserKeywordId(this.id.value),
            keywordId = KeywordId(this.keywordId.value),
            userId = UserId(this.userId.value),
            description = Description(this.description),
            createdAt = this.createdAt.toEpochSecond(),
            updatedAt = this.updatedAt.toEpochSecond(),
        )
}
