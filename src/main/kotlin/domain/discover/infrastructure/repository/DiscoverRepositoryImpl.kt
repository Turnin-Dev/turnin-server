package com.peekr.domain.discover.infrastructure.repository

import com.peekr.common.db.schema.Keywords
import com.peekr.common.db.schema.UserKeywords
import com.peekr.common.db.schema.Users
import com.peekr.common.db.suspendTransaction
import com.peekr.common.model.KeywordName
import com.peekr.common.model.UserName
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.discover.domain.model.SharedUserKeyword
import com.peekr.domain.discover.domain.repository.DiscoverRepository
import org.jetbrains.exposed.sql.DoubleColumnType
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.LongColumnType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.transactions.TransactionManager

class DiscoverRepositoryImpl : DiscoverRepository {
    override suspend fun findUserIdsWithSimilarKeywords(
        targetUserId: UserId,
        cursor: Long?,
        pageSize: Int,
    ): List<UserId> = suspendTransaction {
        val cursorCondition = if (cursor != null) "AND uk_other.user_id < ?" else ""
        val limitPlusOne = pageSize + 1

        val sql = """
            SELECT DISTINCT uk_other.user_id
            FROM user_keyword uk_mine
            JOIN keyword k_mine ON uk_mine.keyword_id = k_mine.id
            JOIN keyword k_other ON (1 - (k_other.embedding <=> k_mine.embedding)) >= ?
            JOIN user_keyword uk_other ON k_other.id = uk_other.keyword_id
            WHERE uk_mine.user_id = ?
                AND uk_mine.is_active = true
                AND uk_other.user_id != ?
                AND uk_other.is_active = true
                AND NOT EXISTS (
                    SELECT 1
                    FROM block
                    WHERE (block.blocker_id = ? AND block.blocked_id = uk_other.user_id)
                        OR (block.blocked_id = ? AND block.blocker_id = uk_other.user_id)
                )
                $cursorCondition
            ORDER BY uk_other.user_id DESC
            LIMIT ?;
        """.trimIndent()

        val params = buildList {
            add(DoubleColumnType() to SharedUserKeyword.HIGH_SIMILARITY_THRESHOLD)
            add(LongColumnType() to targetUserId.value)
            add(LongColumnType() to targetUserId.value)
            add(LongColumnType() to targetUserId.value)
            add(LongColumnType() to targetUserId.value)
            cursor?.let { add(LongColumnType() to it) }
            add(IntegerColumnType() to limitPlusOne)
        }

        val ids = mutableListOf<UserId>()
        TransactionManager.current().exec(sql, params) { rs ->
            while (rs.next()) {
                ids.add(UserId(rs.getLong("user_id")))
            }
        }

        ids
    }

    override suspend fun fetchSharedUserKeywords(
        matchedUserIds: List<UserId>,
    ): List<SharedUserKeyword> = suspendTransaction {
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

        joinQuery
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
