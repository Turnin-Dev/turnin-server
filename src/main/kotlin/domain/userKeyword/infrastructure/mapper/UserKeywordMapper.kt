package com.turnin.domain.userKeyword.infrastructure.mapper

import com.turnin.common.db.schema.Keywords
import com.turnin.common.db.schema.UserKeywordEntity
import com.turnin.common.db.schema.UserKeywords
import com.turnin.common.db.schema.Users
import com.turnin.common.model.KeywordName
import com.turnin.common.model.UserName
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.userKeyword.domain.model.Description
import com.turnin.domain.userKeyword.domain.model.UserInfo
import com.turnin.domain.userKeyword.domain.model.UserKeyword
import com.turnin.domain.userKeyword.domain.model.UserKeywordDetail
import org.jetbrains.exposed.sql.ResultRow

internal object UserKeywordMapper {
    fun ResultRow.toDomain(): UserKeyword =
        UserKeyword(
            id = UserKeywordId(this[UserKeywords.id].value),
            keywordId = KeywordId(this[UserKeywords.keywordId].value),
            userId = UserId(this[UserKeywords.userId].value),
            description = Description(this[UserKeywords.description]),
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

    fun ResultRow.toDetail(): UserKeywordDetail =
        UserKeywordDetail(
            userKeywordId = UserKeywordId(this[UserKeywords.id].value),
            keywordId = KeywordId(this[UserKeywords.keywordId].value),
            keywordName = KeywordName(this[Keywords.keyword]),
            description = Description(this[UserKeywords.description]),
            userInfo = UserInfo(
                userId = UserId(this[Users.id].value),
                userName = UserName(this[Users.name]),
                profileImageUrl = this[Users.profileImageUrl],
            ),
            createdAt = this[UserKeywords.createdAt].toEpochSecond(),
            updatedAt = this[UserKeywords.updatedAt].toEpochSecond(),
        )
}
