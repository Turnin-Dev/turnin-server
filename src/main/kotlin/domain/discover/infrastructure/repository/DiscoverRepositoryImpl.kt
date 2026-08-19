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
import com.turnin.domain.discover.domain.model.DiscoverCursor
import com.turnin.domain.discover.domain.model.SharedUserKeyword
import com.turnin.domain.discover.domain.repository.DiscoverRepository
import com.turnin.domain.discover.domain.repository.DiscoveredResult
import org.jetbrains.exposed.sql.DoubleColumnType
import org.jetbrains.exposed.sql.IColumnType
import org.jetbrains.exposed.sql.IntegerColumnType
import org.jetbrains.exposed.sql.LongColumnType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.VarCharColumnType
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.statements.StatementType
import org.jetbrains.exposed.sql.transactions.TransactionManager

class DiscoverRepositoryImpl : DiscoverRepository {
    override suspend fun findUserIdsWithSimilarKeywords(
        targetUserId: UserId,
        viewerUserId: UserId?,
        seed: String,
        similarityThreshold: Double,
        snapshotAt: Long,
        cursor: DiscoverCursor?,
        pageSize: Int,
    ): List<DiscoveredResult> = suspendTransaction {
        val sql = findUserIdsWithSimilarKeywordsNativeSQL(cursor != null)

        val params = buildList {
            add(LongColumnType() to targetUserId.value) // 1. my_keywords: user_id = ?
            add(DoubleColumnType() to similarityThreshold) // 2. similar_keywords: similarity >= ?
            add(VarCharColumnType() to seed) // 3. shuffle_key seed
            add(LongColumnType() to targetUserId.value) // 4. uk.user_id != ? (본인 제외)
            add(LongColumnType() to viewerUserId?.value) // 5. viewerUserId IS NULL
            add(LongColumnType() to viewerUserId?.value) // 6. uk.user_id != ? (뷰어 제외, 재바인딩)
            add(LongColumnType() to snapshotAt) // 7. uk.updated_at <= ? (스냅샷 필터)
            add(LongColumnType() to targetUserId.value) // 8. blocker_id = ?
            add(LongColumnType() to targetUserId.value) // 9. blocked_id = ?
            add(IntegerColumnType() to cursor?.lastScoreChunk) // 10. ?::integer IS NULL

            // 정렬 방향이 컬럼마다 다르므로(score_chunk ASC, shuffle_key/user_id DESC),
            // 튜플 비교(>) 대신 컬럼별 방향에 맞춘 OR 체인으로 바인딩한다.
            cursor?.let {
                add(IntegerColumnType() to it.lastScoreChunk) // 11. score_chunk > ?
                add(IntegerColumnType() to it.lastScoreChunk) // 12. score_chunk = ?
                add(IntegerColumnType() to it.lastShuffleKey) // 13. shuffle_key < ?
                add(IntegerColumnType() to it.lastScoreChunk) // 14. score_chunk = ?
                add(IntegerColumnType() to it.lastShuffleKey) // 15. shuffle_key = ?
                add(LongColumnType() to it.lastUserId) // 16. user_id < ?
            }

            add(IntegerColumnType() to pageSize) // 17. LIMIT ?
        }

        executeDiscoverQuery(sql, params)
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

    private fun executeDiscoverQuery(
        sql: String,
        params: List<Pair<IColumnType<*>, Any?>>,
    ): List<DiscoveredResult> =
        TransactionManager.current().exec(
            stmt = sql,
            args = params,
            explicitStatementType = StatementType.SELECT,
        ) { rs ->
            val results = mutableListOf<DiscoveredResult>()
            while (rs.next()) {
                results.add(
                    DiscoveredResult(
                        userId = UserId(rs.getLong("user_id")),
                        matchScore = rs.getDouble("match_score"),
                        shuffleKey = rs.getInt("shuffle_key"),
                        scoreChunk = rs.getInt("score_chunk"),
                    ),
                )
            }
            results
        } ?: emptyList()

    private fun findUserIdsWithSimilarKeywordsNativeSQL(hasCursor: Boolean): String {
        // 정렬: score_chunk ASC, shuffle_key DESC, user_id DESC
        // 튜플 비교(>) 대신 컬럼별 방향에 맞춘 OR 체인 사용
        val cursorCondition = if (hasCursor) {
            """
            OR sc.score_chunk > ?
            OR (sc.score_chunk = ? AND sc.shuffle_key < ?)
            OR (sc.score_chunk = ? AND sc.shuffle_key = ? AND sc.user_id < ?)
            """.trimIndent()
        } else {
            ""
        }
        return """
        WITH my_keywords AS MATERIALIZED (
            SELECT
                my_uk.keyword_id,
                k.embedding
            FROM (
                SELECT keyword_id
                FROM user_keyword
                WHERE user_id = ?
                  AND is_active = true
                ORDER BY created_at DESC
                LIMIT 5
            ) my_uk
            JOIN keyword k ON k.id = my_uk.keyword_id
            WHERE k.embedding IS NOT NULL
        ),
        similar_keywords AS MATERIALIZED (
            SELECT DISTINCT ON (candidate_k.id)
                candidate_k.id AS candidate_kw_id,
                (1 - (candidate_k.embedding <=> mk.embedding)) AS similarity
            FROM my_keywords mk
            CROSS JOIN LATERAL (
                SELECT id, embedding
                FROM keyword
                WHERE embedding IS NOT NULL
                ORDER BY embedding <=> mk.embedding, id
                LIMIT 15
            ) candidate_k
            WHERE (1 - (candidate_k.embedding <=> mk.embedding)) >= ?
            ORDER BY candidate_kw_id, similarity DESC
        ),
        candidate_scores AS (
            SELECT
                capped.user_id,
                MAX(capped.similarity) AS match_score,
                (abs(hashtext(? || '-' || capped.user_id::text)::bigint) % 100000) AS shuffle_key
            FROM (
                SELECT sk.candidate_kw_id, sk.similarity, uk.user_id
                FROM similar_keywords sk
                JOIN user_keyword uk ON uk.keyword_id = sk.candidate_kw_id
                WHERE uk.is_active = true
                  AND uk.user_id != ?
                  AND (?::bigint IS NULL OR uk.user_id != ?)
                  AND uk.updated_at <= to_timestamp(? / 1000.0)
                ORDER BY sk.similarity DESC, uk.user_id
                LIMIT 20000	-- 이상 상황 대비 후보 풀 상한 값 (결정적 정렬 후 적용)
            ) capped
            GROUP BY capped.user_id
        ),
        scored_chunks AS (
            SELECT
                cs.user_id,
                cs.match_score,
                cs.shuffle_key,
                NTILE(5) OVER (ORDER BY cs.match_score DESC, cs.shuffle_key DESC, cs.user_id DESC) AS score_chunk
            FROM candidate_scores cs
        ),
        blocked_users AS (
            SELECT blocked_id AS user_id FROM block WHERE blocker_id = ?
            UNION
            SELECT blocker_id AS user_id FROM block WHERE blocked_id = ?
        )
        SELECT
            sc.user_id,
            sc.match_score,
            sc.shuffle_key,
            sc.score_chunk
        FROM scored_chunks sc
        WHERE sc.user_id NOT IN (SELECT user_id FROM blocked_users)
          AND (
              ?::integer IS NULL
              $cursorCondition
          )
        ORDER BY
            sc.score_chunk ASC,
            sc.shuffle_key DESC,
            sc.user_id DESC
        LIMIT ?;
            """.trimIndent()
    }
}
