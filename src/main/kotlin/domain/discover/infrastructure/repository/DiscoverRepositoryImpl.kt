package com.turnin.domain.discover.infrastructure.repository

import com.turnin.common.db.schema.Keywords
import com.turnin.common.db.schema.UserKeywords
import com.turnin.common.db.schema.Users
import com.turnin.common.db.suspendTransaction
import com.turnin.common.model.KeywordName
import com.turnin.common.model.UserName
import com.turnin.common.model.id.DisplayId
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.discover.domain.model.SharedUserKeyword
import com.turnin.domain.discover.domain.repository.DiscoverRepository
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
            add(LongColumnType() to targetUserId.value) // my_categories: user_id = ?
            add(LongColumnType() to targetUserId.value) // candidate_users: user_id != ?
            add(LongColumnType() to targetUserId.value) // blocked_users: blocker_id = ?
            add(LongColumnType() to targetUserId.value) // blocked_users: blocked_id = ?
            cursor?.let { add(LongColumnType() to it) } // cu.user_id < ?
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
        val cursorCondition = if (hasCursor) "AND cu.user_id < ?" else ""
        return """
            WITH my_categories AS MATERIALIZED (
                SELECT DISTINCT k.category
                FROM (
                    SELECT keyword_id
                    FROM user_keyword
                    WHERE user_id = ?
                      AND is_active = true
                    ORDER BY created_at DESC
                    LIMIT 5
                ) my_uk
                JOIN keyword k ON k.id = my_uk.keyword_id
                WHERE k.category IS NOT NULL
            ),
            candidate_users AS MATERIALIZED (
                SELECT DISTINCT uk.user_id
                FROM my_categories mc
                JOIN keyword k ON k.category = mc.category
                JOIN user_keyword uk ON uk.keyword_id = k.id
                WHERE uk.is_active = true
                  AND uk.user_id != ?
            ),
            blocked_users AS MATERIALIZED (
                SELECT blocked_id AS user_id FROM block WHERE blocker_id = ?
                UNION
                SELECT blocker_id AS user_id FROM block WHERE blocked_id = ?
            )
            SELECT cu.user_id
            FROM candidate_users cu
            WHERE cu.user_id NOT IN (SELECT user_id FROM blocked_users)
              $cursorCondition
            ORDER BY cu.user_id DESC
            LIMIT ?;
            """.trimIndent()
    }
}
