package com.peekr.domain.feed.infrastructure.repository

import com.peekr.common.db.suspendTransaction
import com.peekr.common.model.KeywordName
import com.peekr.common.model.UserName
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.common.util.toOffsetDateTime
import com.peekr.domain.feed.domain.model.Feed
import com.peekr.domain.feed.domain.repository.FeedRepository
import com.peekr.domain.userKeyword.domain.model.Description
import java.time.Instant
import org.jetbrains.exposed.sql.DoubleColumnType
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
    ): List<Feed> = suspendTransaction {
        val sql = getFeedsNativeSQL()
        val params = buildList {
            add(LongColumnType() to userId.value) // my_top_keywords: uk.user_id = ?
            add(LongColumnType() to userId.value) // candidate_pool: uk.user_id = ?
            add(LongColumnType() to userId.value) // candidate_pool: block.blocker_id = ?
            add(LongColumnType() to userId.value) // candidate_pool: block.blocked_id = ?
            add(LongColumnType() to userId.value) // friends: requester_id = ?
            add(LongColumnType() to userId.value) // friends: requester_id = ?
            add(LongColumnType() to userId.value) // friends: receiver_id = ?
            add(DoubleColumnType() to cursorScore)
            add(DoubleColumnType() to cursorScore)
            add(JavaOffsetDateTimeColumnType() to cursorCreatedAt?.let { Instant.ofEpochSecond(it).toOffsetDateTime() })
            add(LongColumnType() to cursorUkId?.value)
            add(IntegerColumnType() to limit + 1)
        }

        TransactionManager.current().exec(
            stmt = sql,
            args = params,
            explicitStatementType = StatementType.SELECT,
        ) { rs ->
            val feeds = mutableListOf<Feed>()
            while (rs.next()) {
                val feed = Feed(
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
                )
                feeds.add(feed)
            }
            feeds
        } ?: emptyList()
    }

    private fun getFeedsNativeSQL(): String = """
        with my_top_keywords as (
            select uk.keyword_id, k.embedding as seed_embedding
            from user_keyword uk
            join keyword k on uk.keyword_id = k.id
            where uk.user_id = ? and uk.is_active = true
            order by uk.created_at desc
            LIMIT 5
        ),
        candidate_pool AS (
            select r.*
            from my_top_keywords mtk
            cross join lateral (
                select
                    uk.id as uk_id,
                    uk.user_id as uk_user_id,
                    uk.keyword_id as uk_keyword_id,
                    uk.description as uk_description,
                    uk.created_at as uk_created_at,
                    (1 - (k.embedding <=> mtk.seed_embedding)) as similarity
                FROM keyword k
                JOIN user_keyword uk ON k.id = uk.keyword_id
                WHERE uk.user_id != ?
                    AND uk.is_active = true
                    AND NOT EXISTS (
                        SELECT 1
                        FROM block
                        WHERE (block.blocker_id = ? AND block.blocked_id = uk.user_id)
                            or (block.blocked_id = ? AND block.blocker_id = uk.user_id)
                    )
                ORDER BY k.embedding <=> mtk.seed_embedding
                LIMIT 20
            ) r
        ),
        friends AS (
            select
            	case
        	    	when requester_id = ?
        	    	then receiver_id
        	    	else requester_id
            	end as id
            from friend
            where (requester_id = ? or receiver_id = ?) and status = 'ACCEPTED'
        ),
        scored_pool AS (
            select distinct ON (cp.uk_id)
                cp.*,
                ((cp.similarity * 50) +
                	(CASE
        	        	WHEN cp.uk_user_id IN (select id from friends)
        	        	THEN 100
        	        	ELSE 0
                	END)
            	) as final_score
            FROM candidate_pool cp
            order by cp.uk_id, final_score desc
        ),
        result_list as (
        	SELECT *
        	FROM scored_pool
            WHERE
                (?::float8 IS NULL OR
                 (final_score, uk_created_at, uk_id) < (?::float8, ?, ?))
            ORDER BY final_score DESC, uk_created_at DESC, uk_id DESC
            LIMIT ?
        )
        select
        	rl.*,
        	u.name,
        	u.profile_image_url,
        	k.keyword
        from result_list rl
        join "user" u on rl.uk_user_id = u.id
        join keyword k on rl.uk_keyword_id = k.id
        order by rl.final_score desc, rl.uk_created_at desc, rl.uk_id desc;
        """.trimIndent()
}
