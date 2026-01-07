package com.peekr.domain.discover.infrastructure.repository

import com.peekr.common.db.schema.Keywords
import com.peekr.common.db.schema.UserKeywords
import com.peekr.common.db.schema.Users
import com.peekr.common.model.KeywordName
import com.peekr.common.model.UserName
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.discover.domain.model.SharedUserKeyword
import com.peekr.domain.discover.domain.repository.DiscoverRepository
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.transactions.TransactionManager

class DiscoverRepositoryImpl : DiscoverRepository {
    override fun findUserIdsWithSimilarKeywords(
        targetUserId: UserId,
        cursor: Long?,
        pageSize: Int,
    ): List<UserId> {
        val cursorCondition = if (cursor != null) "AND uk_other.user_id < $cursor" else ""
        val limitPlusOne = pageSize + 1

        val sql = """
            SELECT DISTINCT uk_other.user_id
            FROM user_keyword uk_mine
            JOIN keyword k_mine ON uk_mine.keyword_id = k_mine.id
            JOIN keyword k_other ON (1 - (k_other.embedding <=> k_mine.embedding)) >= ${SharedUserKeyword.HIGH_SIMILARITY_THRESHOLD}
            JOIN user_keyword uk_other ON k_other.id = uk_other.keyword_id
            WHERE uk_mine.user_id = ${targetUserId.value}
              AND uk_other.user_id != ${targetUserId.value}
              $cursorCondition
            ORDER BY uk_other.user_id DESC
            LIMIT $limitPlusOne;
        """.trimIndent()

        val ids = mutableListOf<UserId>()
        TransactionManager.current().exec(sql) { rs ->
            while (rs.next()) {
                ids.add(UserId(rs.getLong("user_id")))
            }
        }

        return ids
    }

    override fun fetchSharedUserKeywords(
        matchedUserIds: List<UserId>,
    ): List<SharedUserKeyword> {
        val matchedUserIdsValue = matchedUserIds.map { it.value }

        val joinQuery = Users
            .innerJoin(
                otherTable = UserKeywords,
                onColumn = { Users.id },
                otherColumn = { UserKeywords.userId },
            ).innerJoin(
                otherTable = Keywords,
                onColumn = { UserKeywords.keywordId },
                otherColumn = { Keywords.id },
            )

        return joinQuery
            .select(
                Users.id,
                Users.name,
                Users.displayId,
                Users.profileImageUrl,
                UserKeywords.id,
                Keywords.id,
                Keywords.keyword,
            ).where { Users.id inList matchedUserIdsValue }
            .orderBy(Users.id to SortOrder.DESC)
            .map { row ->
                SharedUserKeyword(
                    userId = UserId(row[Users.id].value),
                    userName = UserName(row[Users.name]),
                    userDisplayId = DisplayId(row[Users.displayId]),
                    userProfileImageUrl = row[Users.profileImageUrl],
                    userKeywordId = UserKeywordId(row[UserKeywords.id].value),
                    keywordId = KeywordId(row[Keywords.id].value),
                    keywordName = KeywordName(row[Keywords.keyword]),
                )
            }
    }
}
