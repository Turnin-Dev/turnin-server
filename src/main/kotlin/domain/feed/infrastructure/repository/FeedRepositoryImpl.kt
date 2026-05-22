package com.turnin.domain.feed.infrastructure.repository

import com.turnin.common.db.suspendTransaction
import com.turnin.common.model.KeywordName
import com.turnin.common.model.UserName
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.common.util.toOffsetDateTime
import com.turnin.domain.feed.domain.model.Feed
import com.turnin.domain.feed.domain.repository.FeedRepository
import com.turnin.domain.userKeyword.domain.model.Description
import org.jetbrains.exposed.sql.DoubleColumnType
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.LongColumnType
import org.jetbrains.exposed.sql.statements.StatementType
import org.jetbrains.exposed.sql.transactions.TransactionManager

class FeedRepositoryImpl : FeedRepository {
    override suspend fun getFeeds(
        userId: UserId,
        cursorScore: Double?,
        cursorUkId: UserKeywordId?,
        limit: Int,
        similarPoolLimit: Int,
        fallbackPoolLimit: Int,
    ): List<Feed> = suspendTransaction {
        val sql = getFeedsNativeSQL()
        val params = buildList {
            // my_top_keywords
            add(LongColumnType() to userId.value) // uk.user_id = ?

            // friends
            add(LongColumnType() to userId.value) // requester_id = ?
            add(LongColumnType() to userId.value) // requester_id = ? OR receiver_id = ?
            add(LongColumnType() to userId.value) // receiver_id = ?  (CASE WHEN)

            // blocked_users
            add(LongColumnType() to userId.value) // blocker_id = ?
            add(LongColumnType() to userId.value) // blocked_id = ?

            // similar_pool
            add(LongColumnType() to userId.value) // uk.user_id != ?
            add(IntegerColumnType() to similarPoolLimit) // LIMIT ?

            // fallback_pool
            add(LongColumnType() to userId.value) // uk.user_id != ?
            add(IntegerColumnType() to fallbackPoolLimit) // LIMIT ?

            // result_list
            add(DoubleColumnType() to cursorScore) // IS NULL
            add(DoubleColumnType() to cursorScore) // final_score
            add(DoubleColumnType() to cursorScore) // final_score =
            add(LongColumnType() to cursorUkId?.value) // uk_id
            add(IntegerColumnType() to limit) // LIMIT
        }

        executeFeedQuery(sql, params)
    }

    override suspend fun getFallbackFeeds(
        userId: UserId,
        cursorUkId: UserKeywordId?,
        limit: Int,
    ): List<Feed> = suspendTransaction {
        val sql = getFallbackFeedsNativeSQL()
        val params = buildList {
            add(LongColumnType() to userId.value) // uk.user_id != ?
            add(LongColumnType() to userId.value) // blocker_id = ?
            add(LongColumnType() to userId.value) // blocked_id = ?
            add(LongColumnType() to cursorUkId?.value) // ?::bigint IS NULL
            add(LongColumnType() to cursorUkId?.value) // uk.id < ?
            add(IntegerColumnType() to limit) // LIMIT ?
        }

        executeFeedQuery(sql, params)
    }

    private fun getFeedsNativeSQL(): String = """
        WITH my_top_keywords AS (
            SELECT uk.keyword_id, k.category
            FROM user_keyword uk
            JOIN keyword k ON uk.keyword_id = k.id
            WHERE uk.user_id = ?
              AND uk.is_active = true
              AND k.category IS NOT NULL
            ORDER BY uk.created_at DESC
            LIMIT 5
        ),
        my_categories AS MATERIALIZED (
            SELECT DISTINCT category FROM my_top_keywords
        ),
        friends AS (
            SELECT
                CASE
                    WHEN requester_id = ? THEN receiver_id
                    ELSE requester_id
                END AS id
            FROM friend
            WHERE (requester_id = ? OR receiver_id = ?)
              AND status = 'ACCEPTED'
        ),
        blocked_users AS MATERIALIZED (
            SELECT blocked_id AS user_id FROM block WHERE blocker_id = ?
            UNION
            SELECT blocker_id AS user_id FROM block WHERE blocked_id = ?
        ),
        friend_pool AS (
            SELECT
                uk.id AS uk_id,
                uk.user_id AS uk_user_id,
                uk.keyword_id AS uk_keyword_id,
                uk.description AS uk_description,
                uk.created_at AS uk_created_at,
                1 AS priority,
                0.0 AS similarity  -- 친구는 similarity 미사용
            FROM user_keyword uk
            WHERE uk.user_id IN (SELECT id FROM friends)
              AND uk.is_active = true
              AND NOT EXISTS (SELECT 1 FROM blocked_users b WHERE b.user_id = uk.user_id)
        ),
        similar_pool AS (
            SELECT
                uk.id AS uk_id,
                uk.user_id AS uk_user_id,
                uk.keyword_id AS uk_keyword_id,
                uk.description AS uk_description,
                uk.created_at AS uk_created_at,
                2 AS priority,
                0.5 AS similarity
            FROM my_categories mc  -- my_categories가 비어있으면 조인 결과도 0행
            JOIN keyword k ON k.category = mc.category
            JOIN user_keyword uk ON uk.keyword_id = k.id
            WHERE uk.is_active = true
              AND uk.user_id != ?
              AND NOT EXISTS (SELECT 1 FROM friends f WHERE f.id = uk.user_id)
              AND NOT EXISTS (SELECT 1 FROM blocked_users b WHERE b.user_id = uk.user_id)
              AND EXISTS (SELECT 1 FROM my_categories)  -- 빈 경우 즉시 종료
            ORDER BY uk.created_at DESC, uk.id DESC
            LIMIT ?
        ),
        fallback_pool AS (
            SELECT
                uk.id AS uk_id,
                uk.user_id AS uk_user_id,
                uk.keyword_id AS uk_keyword_id,
                uk.description AS uk_description,
                uk.created_at AS uk_created_at,
                3 AS priority,
                0.0 AS similarity
            FROM user_keyword uk
            WHERE uk.user_id != ?
              AND uk.is_active = true
              AND NOT EXISTS (SELECT 1 FROM friends f WHERE f.id = uk.user_id)
              AND NOT EXISTS (SELECT 1 FROM blocked_users b WHERE b.user_id = uk.user_id)
            ORDER BY uk.created_at DESC, uk.id DESC
            LIMIT ?
        ),
        combined AS (
            SELECT * FROM friend_pool
            UNION ALL
            SELECT * FROM similar_pool
            UNION ALL
            SELECT * FROM fallback_pool
        ),
        scored_pool AS (
            SELECT DISTINCT ON (uk_id)
                *,
                (
                    (similarity * 50) +
                    (CASE WHEN priority = 1 THEN 100 ELSE 0 END)
                ) AS final_score
            FROM combined
            ORDER BY uk_id, priority ASC
        ),
        result_list AS (
            SELECT
                uk_id, uk_user_id, uk_keyword_id, uk_description, uk_created_at,
                similarity, final_score
            FROM scored_pool
            WHERE (?::float8 IS NULL OR
                final_score < ?::float8 OR
                (final_score = ?::float8 AND uk_id < ?))
            ORDER BY final_score DESC, uk_id DESC
            LIMIT ?
        )
        SELECT
            rl.uk_id, rl.uk_user_id, rl.uk_keyword_id, rl.uk_description, rl.uk_created_at,
            rl.similarity, rl.final_score,
            u.name, u.profile_image_url,
            k.keyword
        FROM result_list rl
        JOIN "user" u ON rl.uk_user_id = u.id
        JOIN keyword k ON rl.uk_keyword_id = k.id
        ORDER BY rl.final_score DESC, rl.uk_id DESC;
        """.trimIndent()

    private fun getFallbackFeedsNativeSQL(): String = """
        SELECT
            uk.id AS uk_id,
            uk.user_id AS uk_user_id,
            uk.keyword_id AS uk_keyword_id,
            uk.description AS uk_description,
            uk.created_at AS uk_created_at,
            0.0 AS similarity,
            0.0 AS final_score,
            u.name,
            u.profile_image_url,
            k.keyword
        FROM user_keyword uk
        JOIN "user" u ON uk.user_id = u.id
        JOIN keyword k ON uk.keyword_id = k.id
        WHERE uk.user_id != ?
            AND uk.is_active = true
            AND NOT EXISTS (
                SELECT 1 FROM block
                WHERE (block.blocker_id = ? AND block.blocked_id = uk.user_id)
                    OR (block.blocked_id = ? AND block.blocker_id = uk.user_id)
            )
            AND (?::bigint IS NULL OR uk.id < ?)
        ORDER BY uk.id DESC
        LIMIT ?;
        """.trimIndent()

    private fun executeFeedQuery(
        sql: String,
        params: List<Pair<IColumnType<*>, Any?>>,
    ): List<Feed> =
        TransactionManager.current().exec(
            stmt = sql,
            args = params,
            explicitStatementType = StatementType.SELECT,
        ) { rs ->
            val feeds = mutableListOf<Feed>()
            while (rs.next()) {
                feeds.add(
                    Feed(
                        userKeywordId = UserKeywordId(rs.getLong("uk_id")),
                        userId = UserId(rs.getLong("uk_user_id")),
                        userName = UserName(rs.getString("name")),
                        keywordId = KeywordId(rs.getLong("uk_keyword_id")),
                        profileImageUrl = rs.getString("profile_image_url"),
                        keyword = KeywordName(rs.getString("keyword")),
                        description = Description(rs.getString("uk_description")),
                        createdAt = rs
                            .getTimestamp("uk_created_at")
                            .toInstant()
                            .toOffsetDateTime()
                            .toEpochSecond(),
                        score = rs.getDouble("final_score"),
                        similarity = rs.getDouble("similarity"),
                    ),
                )
            }
            feeds
        } ?: emptyList()
}
