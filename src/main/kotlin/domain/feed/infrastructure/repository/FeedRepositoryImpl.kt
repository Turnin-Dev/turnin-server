package com.turnin.domain.feed.infrastructure.repository

import com.turnin.common.db.suspendTransaction
import com.turnin.common.model.KeywordName
import com.turnin.common.model.UserName
import com.turnin.common.model.id.KeywordId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.common.util.toOffsetDateTime
import com.turnin.domain.feed.domain.model.Feed
import com.turnin.domain.feed.domain.model.FeedRow
import com.turnin.domain.feed.domain.model.FeedWindowResult
import com.turnin.domain.feed.domain.repository.FeedRepository
import com.turnin.domain.userKeyword.domain.model.Description
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.LongColumnType
import org.jetbrains.exposed.sql.VarCharColumnType
import org.jetbrains.exposed.sql.statements.StatementType
import org.jetbrains.exposed.sql.transactions.TransactionManager

class FeedRepositoryImpl : FeedRepository {
    override suspend fun getFriendFeeds(
        userId: UserId,
        seed: String,
        sessionMaxId: Long?,
        windowAnchorId: UserKeywordId?,
        lastShuffleKey: Int?,
        lastUkId: Long?,
        windowSize: Int,
        limit: Int,
    ): FeedWindowResult = suspendTransaction {
        val sql = getFriendFeedsNativeSQL()
        val params = buildList {
            // friends
            add(LongColumnType() to userId.value) // requester_id = ? (CASE)
            add(LongColumnType() to userId.value) // requester_id = ?
            add(LongColumnType() to userId.value) // receiver_id = ?

            // blocked_users
            add(LongColumnType() to userId.value) // blocker_id = ?
            add(LongColumnType() to userId.value) // blocked_id = ?

            // params: session_max_id
            add(LongColumnType() to sessionMaxId) // COALESCE(?::bigint, MAX(id))

            // window_pool anchor
            add(LongColumnType() to windowAnchorId?.value) // ?::bigint IS NULL
            add(LongColumnType() to windowAnchorId?.value) // uk.id < ?
            add(IntegerColumnType() to windowSize) // LIMIT ?

            // shuffled: shuffle_key 계산 (딱 한 번만 사용)
            add(VarCharColumnType() to seed) // hashtext(? || '-' || uk_id)

            // 커서 필터 (offset 대체)
            add(IntegerColumnType() to lastShuffleKey) // ?::int IS NULL
            add(IntegerColumnType() to lastShuffleKey) // (shuffle_key, uk_id) > (?, ?)
            add(LongColumnType() to lastUkId)

            // page size
            add(IntegerColumnType() to limit) // LIMIT ?
        }

        executeFeedWindowQuery(sql, params)
    }

    override suspend fun getAllFeeds(
        userId: UserId,
        seed: String,
        sessionMaxId: Long?,
        windowAnchorId: UserKeywordId?,
        lastShuffleKey: Int?,
        lastUkId: Long?,
        windowSize: Int,
        limit: Int,
    ): FeedWindowResult = suspendTransaction {
        val sql = getAllFeedsNativeSQL()
        val params = buildList {
            // blocked_users
            add(LongColumnType() to userId.value) // blocker_id = ?
            add(LongColumnType() to userId.value) // blocked_id = ?

            // params: session_max_id
            add(LongColumnType() to sessionMaxId) // COALESCE(?::bigint, MAX(id))

            // window_pool
            add(LongColumnType() to userId.value) // uk.user_id != ?
            add(LongColumnType() to windowAnchorId?.value) // ?::bigint IS NULL
            add(LongColumnType() to windowAnchorId?.value) // uk.id < ?
            add(IntegerColumnType() to windowSize) // LIMIT ?

            // shuffled: shuffle_key 계산
            add(VarCharColumnType() to seed)

            // 커서 필터
            add(IntegerColumnType() to lastShuffleKey) // ?::int IS NULL
            add(IntegerColumnType() to lastShuffleKey) // (shuffle_key, uk_id) > (?, ?)
            add(LongColumnType() to lastUkId)

            // page size
            add(IntegerColumnType() to limit)
        }

        executeFeedWindowQuery(sql, params)
    }

    private fun getFriendFeedsNativeSQL(): String = """
        WITH friends AS (
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
        params AS (
            -- 세션 시작 시점의 최신 id 스냅샷. 커서에 값이 있으면 그대로 쓰고,
            -- 첫 요청(null)일 때만 현재 MAX(id)를 계산해 이후 페이지에 고정 전달
            SELECT COALESCE(?::bigint, (SELECT MAX(id) FROM user_keyword)) AS session_max_id
        ),
        window_pool AS (
            SELECT
                uk.id AS uk_id,
                uk.user_id AS uk_user_id,
                uk.keyword_id AS uk_keyword_id,
                uk.description AS uk_description,
                uk.created_at AS uk_created_at
            FROM user_keyword uk, params p
            WHERE uk.user_id IN (SELECT id FROM friends)
              AND uk.is_active = true
              AND NOT EXISTS (SELECT 1 FROM blocked_users b WHERE b.user_id = uk.user_id)
              AND uk.id <= p.session_max_id
              AND (?::bigint IS NULL OR uk.id < ?)
            ORDER BY uk.id DESC
            LIMIT ?
        ),
        -- 윈도우 함수는 여기서 "청크 전체"에 대해 먼저 계산.
        -- 바깥에서 커서로 WHERE 필터링을 해도 window_fetched_count/window_min_uk_id는
        -- 청크 전체 기준값을 그대로 유지해야 하기 때문 (아래서 필터링하면 값이 오염됨)
        shuffled AS (
            SELECT
                wp.uk_id, wp.uk_user_id, wp.uk_keyword_id, wp.uk_description, wp.uk_created_at,
                u.name, u.profile_image_url, k.keyword,
                (abs(hashtext(? || '-' || wp.uk_id::text)) % 100000) AS shuffle_key,
                MIN(wp.uk_id) OVER () AS window_min_uk_id,
                COUNT(*)      OVER () AS window_fetched_count,
                (SELECT session_max_id FROM params) AS session_max_id
            FROM window_pool wp
            JOIN "user" u ON wp.uk_user_id = u.id
            JOIN keyword k ON wp.uk_keyword_id = k.id
        )
        SELECT *
        FROM shuffled
        WHERE (
            -- (shuffle_key, uk_id) 튜플 커서로 다음 페이지를 특정
            ?::int IS NULL OR (shuffle_key, uk_id) > (?, ?)
        )
        ORDER BY shuffle_key, uk_id
        LIMIT ?;
        """.trimIndent()

    private fun getAllFeedsNativeSQL(): String = """
        WITH blocked_users AS MATERIALIZED (
            SELECT blocked_id AS user_id FROM block WHERE blocker_id = ?
            UNION
            SELECT blocker_id AS user_id FROM block WHERE blocked_id = ?
        ),
        params AS (
            SELECT COALESCE(?::bigint, (SELECT MAX(id) FROM user_keyword)) AS session_max_id
        ),
        window_pool AS (
            SELECT
                uk.id AS uk_id,
                uk.user_id AS uk_user_id,
                uk.keyword_id AS uk_keyword_id,
                uk.description AS uk_description,
                uk.created_at AS uk_created_at
            FROM user_keyword uk, params p
            WHERE uk.user_id != ?
              AND uk.is_active = true
              AND NOT EXISTS (SELECT 1 FROM blocked_users b WHERE b.user_id = uk.user_id)
              AND uk.id <= p.session_max_id
              AND (?::bigint IS NULL OR uk.id < ?)
            ORDER BY uk.id DESC
            LIMIT ?
        ),
        shuffled AS (
            SELECT
                wp.uk_id, wp.uk_user_id, wp.uk_keyword_id, wp.uk_description, wp.uk_created_at,
                u.name, u.profile_image_url, k.keyword,
                (abs(hashtext(? || '-' || wp.uk_id::text)) % 100000) AS shuffle_key,
                MIN(wp.uk_id) OVER () AS window_min_uk_id,
                COUNT(*)      OVER () AS window_fetched_count,
                (SELECT session_max_id FROM params) AS session_max_id
            FROM window_pool wp
            JOIN "user" u ON wp.uk_user_id = u.id
            JOIN keyword k ON wp.uk_keyword_id = k.id
        )
        SELECT *
        FROM shuffled
        WHERE (
            ?::int IS NULL
            OR (shuffle_key, uk_id) > (?, ?)
        )
        ORDER BY shuffle_key, uk_id
        LIMIT ?;
        """.trimIndent()

    private fun executeFeedWindowQuery(
        sql: String,
        params: List<Pair<IColumnType<*>, Any?>>,
    ): FeedWindowResult =
        TransactionManager.current().exec(
            stmt = sql,
            args = params,
            explicitStatementType = StatementType.SELECT,
        ) { rs ->
            val feedRows = mutableListOf<FeedRow>()
            var windowMinUkId: Long? = null
            var windowFetchedCount = 0
            var sessionMaxId: Long? = null

            while (rs.next()) {
                feedRows.add(
                    FeedRow(
                        feed = Feed(
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
                        ),
                        // 다음 페이지 커서를 위해 "이번 응답의 마지막 행" 값을 계속 갱신
                        shuffleKey = rs.getInt("shuffle_key").also {
                            check(!rs.wasNull()) { "shuffle_key must not be null" }
                        },
                    ),
                )

                // 윈도우 함수 / 세션 값은 모든 행에 동일하게 붙어오므로 한 번만 읽어도 됨
                windowMinUkId = rs.getLong("window_min_uk_id").let { if (rs.wasNull()) null else it }
                windowFetchedCount = rs.getInt("window_fetched_count")
                sessionMaxId = rs.getLong("session_max_id").let { if (rs.wasNull()) null else it }
            }

            FeedWindowResult(
                feedsRows = feedRows,
                windowMinUkId = windowMinUkId,
                windowFetchedCount = windowFetchedCount,
                sessionMaxId = sessionMaxId,
            )
        } ?: FeedWindowResult(emptyList(), null, 0, null)
}
