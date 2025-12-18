package com.peekr.domain.keywordGraph.infrastructure.repository

import com.peekr.common.db.StringAgg
import com.peekr.common.db.schema.UserKeywords
import com.peekr.common.db.suspendTransaction
import com.peekr.common.model.id.KeywordId
import com.peekr.common.model.id.UserId
import com.peekr.domain.keywordGraph.domain.model.SharedKeywordInfo
import com.peekr.domain.keywordGraph.domain.repository.KeywordGraphRepository
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.alias
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.innerJoin

class KeywordGraphRepositoryImpl : KeywordGraphRepository {
    override suspend fun getSharedKeywordInfos(
        userId: UserId,
        cursor: Long,
        pageSize: Int,
    ): List<SharedKeywordInfo> = suspendTransaction {
        val uk1 = UserKeywords.alias("uk1")
        val uk2 = UserKeywords.alias("uk2")

        // 1) 집계 함수 인스턴스 생성
        val sharedKeywords = StringAgg(
            expr = uk2[UserKeywords.keywordId],
            delimiter = ",",
            orderBy = uk2[UserKeywords.keywordId],
        )

        // 2) 쿼리 실행
        val query = uk1
            .innerJoin(
                otherTable = uk2,
                onColumn = { uk1[UserKeywords.keywordId] },
                otherColumn = { uk2[UserKeywords.keywordId] },
            ).select(uk2[UserKeywords.userId], sharedKeywords)
            .where {
                (uk1[UserKeywords.userId] eq userId.value) and
                    (uk2[UserKeywords.userId] neq userId.value) and
                    (uk2[UserKeywords.userId] less cursor)
            }.groupBy(uk2[UserKeywords.userId])
            .orderBy(uk2[UserKeywords.userId] to SortOrder.DESC)
            .limit(pageSize)

        // 3) 결과 매핑
        val result = query.map { row ->
            val otherUserId = row[uk2[UserKeywords.userId]].value
            val keywordIds = row[sharedKeywords]?.split(",")?.map { it.toLong() }
                ?: emptyList()

            SharedKeywordInfo(
                userId = UserId(otherUserId),
                keywordIds = keywordIds.map { KeywordId(it) },
            )
        }
        result
    }
}
