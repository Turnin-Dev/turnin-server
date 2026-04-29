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
import java.time.Instant
import org.jetbrains.exposed.sql.DoubleColumnType
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.LongColumnType
import org.jetbrains.exposed.sql.javatime.JavaOffsetDateTimeColumnType
import org.jetbrains.exposed.sql.statements.StatementType
import org.jetbrains.exposed.sql.transactions.TransactionManager

class FeedRepositoryImpl : FeedRepository {
    override suspend fun getFeeds(
        userId: UserId,
        cursorScore: Double?,
        cursorCreatedAt: Long?,
        cursorUkId: UserKeywordId?,
        limit: Int,
        similarityThreshold: Double,
        similarPoolLimit: Int,
        fallbackPoolLimit: Int,
    ): List<Feed> = suspendTransaction {
        val sql = getFeedsNativeSQL()
        val params = buildList {
            add(LongColumnType() to userId.value) // my_top_keywords: uk.user_id = ?
            add(LongColumnType() to userId.value) // friends: requester_id = ?
            add(LongColumnType() to userId.value) // friends: requester_id or receiver_id = ?
            add(LongColumnType() to userId.value) // friends: receiver_id = ?
            add(LongColumnType() to userId.value) // similar_pool: uk.user_id != ?
            add(LongColumnType() to userId.value) // similar_pool: block.blocker_id = ?
            add(LongColumnType() to userId.value) // similar_pool: block.blocked_id = ?
            add(IntegerColumnType() to similarPoolLimit) // similar_pool: LIMIT ?
            add(DoubleColumnType() to similarityThreshold) // similar_pool: similarity >= ?
            add(LongColumnType() to userId.value) // fallback_pool: uk.user_id != ?
            add(LongColumnType() to userId.value) // fallback_pool: block.blocker_id = ?
            add(LongColumnType() to userId.value) // fallback_pool: block.blocked_id = ?
            add(LongColumnType() to cursorCreatedAt) // fallback_pool: ?::bigint IS NULL
            // fallback_pool: uk.created_at < ?
            add(JavaOffsetDateTimeColumnType() to cursorCreatedAt?.let { Instant.ofEpochSecond(it).toOffsetDateTime() })
            add(IntegerColumnType() to fallbackPoolLimit) // fallback_pool: LIMIT ?
            add(DoubleColumnType() to cursorScore) // result_list: cursor null check
            add(DoubleColumnType() to cursorScore) // result_list: final_score < ?
            // result_list: uk_created_at < ?
            add(JavaOffsetDateTimeColumnType() to cursorCreatedAt?.let { Instant.ofEpochSecond(it).toOffsetDateTime() })
            add(LongColumnType() to cursorUkId?.value) // result_list: uk_id < ?
            add(IntegerColumnType() to limit) // result_list: LIMIT ?
        }

        executeFeedQuery(sql, params)
    }

    override suspend fun getFallbackFeeds(
        userId: UserId,
        cursorCreatedAt: Long?,
        limit: Int,
    ): List<Feed> = suspendTransaction {
        val sql = getFallbackFeedsNativeSQL()
        val params = buildList {
            add(LongColumnType() to userId.value) // uk.user_id != ?
            add(LongColumnType() to userId.value) // block.blocker_id = ?
            add(LongColumnType() to userId.value) // block.blocked_id = ?
            add(LongColumnType() to cursorCreatedAt) // cursor null check
            add(
                JavaOffsetDateTimeColumnType() to cursorCreatedAt?.let {
                    Instant.ofEpochSecond(it).toOffsetDateTime()
                },
            ) // uk.created_at < ?
            add(IntegerColumnType() to limit) // LIMIT ?
        }

        executeFeedQuery(sql, params)
    }

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

    private fun getFeedsNativeSQL(): String = """
        WITH my_top_keywords AS (
            SELECT uk.keyword_id, k.embedding AS seed_embedding
            FROM user_keyword uk
            JOIN keyword k ON uk.keyword_id = k.id
            WHERE uk.user_id = ? AND uk.is_active = true
            ORDER BY uk.created_at DESC
            LIMIT 5
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
        friend_pool AS (
            SELECT
                uk.id AS uk_id,
                uk.user_id AS uk_user_id,
                uk.keyword_id AS uk_keyword_id,
                uk.description AS uk_description,
                uk.created_at AS uk_created_at,
                1 AS priority,
                (SELECT COALESCE(MAX(1 - (k.embedding <=> mtk.seed_embedding)), 0)
                 FROM my_top_keywords mtk) AS similarity
            FROM user_keyword uk
            JOIN keyword k ON uk.keyword_id = k.id
            WHERE uk.user_id IN (SELECT id FROM friends)
                AND uk.is_active = true
        ),
        similar_pool AS (
            SELECT
                r.id AS uk_id,
                r.user_id AS uk_user_id,
                r.keyword_id AS uk_keyword_id,
                r.description AS uk_description,
                r.created_at AS uk_created_at,
                2 AS priority,
                r.similarity
            FROM my_top_keywords mtk
            CROSS JOIN LATERAL (
                SELECT
                    uk.id, uk.user_id, uk.keyword_id, uk.description, uk.created_at,
                    (1 - (k.embedding <=> mtk.seed_embedding)) AS similarity
                FROM keyword k
                JOIN user_keyword uk ON k.id = uk.keyword_id
                WHERE uk.user_id != ?
                    AND uk.is_active = true
                    AND uk.user_id NOT IN (SELECT id FROM friends)
                    AND NOT EXISTS (
                        SELECT 1 FROM block
                        WHERE (block.blocker_id = ? AND block.blocked_id = uk.user_id)
                            OR (block.blocked_id = ? AND block.blocker_id = uk.user_id)
                    )
                ORDER BY k.embedding <=> mtk.seed_embedding
                LIMIT ?
            ) r
            WHERE r.similarity >= ?
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
                AND uk.user_id NOT IN (SELECT id FROM friends)
                AND NOT EXISTS (
                    SELECT 1 FROM block
                    WHERE (block.blocker_id = ? AND block.blocked_id = uk.user_id)
                        OR (block.blocked_id = ? AND block.blocker_id = uk.user_id)
                )
                AND (?::bigint IS NULL OR uk.created_at < ?)
            ORDER BY uk.created_at DESC
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
                (final_score, uk_created_at, uk_id) < (?::float8, ?, ?))
            ORDER BY final_score DESC, uk_created_at DESC, uk_id DESC
            LIMIT ?
        )
        SELECT
            rl.uk_id, rl.uk_user_id, rl.uk_keyword_id, rl.uk_description, rl.uk_created_at,
            rl.similarity, rl.final_score,
            u.name, u.profile_image_url, k.keyword
        FROM result_list rl
        JOIN "user" u ON rl.uk_user_id = u.id
        JOIN keyword k ON rl.uk_keyword_id = k.id
        ORDER BY rl.final_score DESC, rl.uk_created_at DESC, rl.uk_id DESC;
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
            AND (?::bigint IS NULL OR uk.created_at < ?)
        ORDER BY uk.created_at DESC
        LIMIT ?;
        """.trimIndent()
}
