package com.peekr.domain.keywordGraph.infrastructure.repository

import com.peekr.common.db.StringAgg
import com.peekr.common.db.schema.UserKeywords
import com.peekr.common.db.suspendTransaction
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.common.model.id.UserKeywordId
import com.peekr.common.util.pagination.cursor.CursorPage
import com.peekr.domain.keywordGraph.domain.model.SharedKeywordInfo
import com.peekr.domain.keywordGraph.domain.repository.KeywordGraphRepository
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.TextColumnType
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.castTo
import org.jetbrains.exposed.sql.innerJoin

class KeywordGraphRepositoryImpl : KeywordGraphRepository {
    override suspend fun getSharedKeywordInfos(
        userId: UserId,
        cursor: Long?,
        pageSize: Int,
    ): CursorPage<SharedKeywordInfo> = suspendTransaction {
        val uk1 = UserKeywords.alias("uk1")
        val uk2 = UserKeywords.alias("uk2")

        // 1) 집계 함수 인스턴스 생성
        val sharedUserKeywordIds = StringAgg(
            expr = uk2[UserKeywords.id].castTo(TextColumnType()),
            delimiter = ",",
            orderBy = uk2[UserKeywords.keywordId],
        )

        val sharedKeywordIds = StringAgg(
            expr = uk2[UserKeywords.keywordId].castTo(TextColumnType()),
            delimiter = ",",
            orderBy = uk2[UserKeywords.keywordId],
        )

        // 2) 쿼리 실행
        val query = uk1
            .innerJoin(
                otherTable = uk2,
                onColumn = { uk1[UserKeywords.keywordId] },
                otherColumn = { uk2[UserKeywords.keywordId] },
            ).select(uk2[UserKeywords.userId], sharedUserKeywordIds, sharedKeywordIds)
            .where {
                val conditions = mutableListOf<Op<Boolean>>()
                conditions.add(uk1[UserKeywords.userId] eq userId.value)
                conditions.add(uk2[UserKeywords.userId] neq userId.value)
                cursor?.let {
                    conditions.add(uk2[UserKeywords.userId] less cursor)
                }

                conditions.reduce { acc, op -> acc and op }
            }.groupBy(uk2[UserKeywords.userId])
            .orderBy(uk2[UserKeywords.userId] to SortOrder.DESC)
            .limit(pageSize + 1)

        // 3) 결과 매핑
        val items = query.map { row ->
            val otherUserId = row[uk2[UserKeywords.userId]].value
            val userKeywordIds = row[sharedUserKeywordIds]?.split(",")?.map { it.toLong() }
                ?: emptyList()
            val keywordIds = row[sharedKeywordIds]?.split(",")?.map { it.toLong() }
                ?: emptyList()

            SharedKeywordInfo(
                userId = UserId(otherUserId),
                userKeywordIds = userKeywordIds.map { UserKeywordId(it) },
                keywordIds = keywordIds.map { KeywordId(it) },
            )
        }

        // 4) 다음 커서 계산
        val hasNext = items.size > pageSize
        val nextCursor = if (hasNext) items[pageSize - 1].userId.value else null
        val resultItems = if (hasNext) items.take(pageSize) else items

        // 5) 최종 반환
        CursorPage(
            items = resultItems,
            nextCursor = nextCursor,
        )
    }
}
