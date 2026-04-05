package com.peekr.domain.discover.infrastructure.repository

import com.peekr.common.db.schema.Keywords
import com.peekr.common.db.schema.UserKeywords
import com.peekr.common.db.schema.Users
import com.peekr.common.db.suspendTransaction
import com.peekr.common.model.KeywordName
import com.peekr.common.model.KeywordSimilarityValues
import com.peekr.common.model.UserName
import com.peekr.common.model.id.DisplayId
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.discover.domain.model.SharedUserKeyword
import com.peekr.domain.discover.domain.repository.DiscoverRepository
import org.jetbrains.exposed.sql.DoubleColumnType
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.LongColumnType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.statements.StatementType
import org.jetbrains.exposed.sql.transactions.TransactionManager

class DiscoverRepositoryImpl : DiscoverRepository {
    override suspend fun findUserIdsWithSimilarKeywords(
        targetUserId: UserId,
        cursor: Long?,
        pageSize: Int,
    ): List<UserId> = suspendTransaction {
        val sql = findUserIdsWithSimilarKeywordsNativeSQL(cursor != null)

        val params = buildList {
            add(LongColumnType() to targetUserId.value) // my_keywords: uk.user_id = ?
            add(DoubleColumnType() to KeywordSimilarityValues.HIGH_THRESHOLD) // similar_ids: similarity >= ?
            add(LongColumnType() to targetUserId.value) // matched_users: uk_other.user_id != ?
            add(LongColumnType() to targetUserId.value) // matched_users: block.blocker_id = ?
            add(LongColumnType() to targetUserId.value) // matched_users: block.blocked_id = ?
            cursor?.let { add(LongColumnType() to it) } // user_id < ?
            add(IntegerColumnType() to pageSize) // LIMIT ?
        }

        executeUserIdQuery(sql, params)
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

    private fun executeUserIdQuery(
        sql: String,
        params: List<Pair<IColumnType<*>, Any?>>,
    ): List<UserId> =
        TransactionManager.current().exec(
            stmt = sql,
            args = params,
            explicitStatementType = StatementType.SELECT,
        ) { rs ->
            val ids = mutableListOf<UserId>()
            while (rs.next()) {
                ids.add(UserId(rs.getLong("user_id")))
            }
            ids
        } ?: emptyList()

    private fun findUserIdsWithSimilarKeywordsNativeSQL(hasCursor: Boolean): String {
        val cursorCondition = if (hasCursor) "AND user_id < ?" else ""
        return """
            WITH my_keywords AS MATERIALIZED (
                SELECT uk.keyword_id, k.embedding
                FROM user_keyword uk
                JOIN keyword k ON uk.keyword_id = k.id
                WHERE uk.user_id = ?
                  AND uk.is_active = true
                ORDER BY uk.created_at DESC
                LIMIT 5
            ),
            similar_ids AS MATERIALIZED (
                SELECT DISTINCT k_other.id
                FROM my_keywords
                CROSS JOIN LATERAL (
                    SELECT id, (1 - (embedding <=> my_keywords.embedding)) AS similarity
                    FROM keyword
                    WHERE id != my_keywords.keyword_id
                    ORDER BY embedding <=> my_keywords.embedding
                    LIMIT 100
                ) k_other
                WHERE k_other.similarity >= ?
            ),
            matched_users AS MATERIALIZED (
                SELECT DISTINCT uk_other.user_id
                FROM similar_ids
                JOIN user_keyword uk_other ON uk_other.keyword_id = similar_ids.id
                WHERE uk_other.is_active = true
                  AND uk_other.user_id != ?
                  AND NOT EXISTS (
                      SELECT 1 FROM block
                      WHERE block.blocker_id = ? AND block.blocked_id = uk_other.user_id
                      UNION ALL
                      SELECT 1 FROM block
                      WHERE block.blocked_id = ? AND block.blocker_id = uk_other.user_id
                  )
            )
            SELECT user_id
            FROM matched_users
            WHERE (1=1)
              $cursorCondition
            ORDER BY user_id DESC
            LIMIT ?;
            """.trimIndent()
    }
}
