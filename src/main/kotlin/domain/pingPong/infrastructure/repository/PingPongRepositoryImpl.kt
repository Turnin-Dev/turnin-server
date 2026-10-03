package com.turnin.domain.pingPong.infrastructure.repository

import com.turnin.common.db.DatabaseUtils.isNotBlockedRelationship
import com.turnin.common.db.extension.filterActiveUser
import com.turnin.common.db.schema.PingPongAnswerEntity
import com.turnin.common.db.schema.PingPongAnswers
import com.turnin.common.db.schema.PingPongEntity
import com.turnin.common.db.schema.PingPongs
import com.turnin.common.db.schema.UserKeywords
import com.turnin.common.db.schema.Users
import com.turnin.common.db.suspendTransaction
import com.turnin.common.model.id.PingPongId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.pingPong.domain.model.PingPong
import com.turnin.domain.pingPong.domain.model.PingPongAnswer
import com.turnin.domain.pingPong.domain.model.PingPongContent
import com.turnin.domain.pingPong.domain.model.PingPongDetail
import com.turnin.domain.pingPong.domain.repository.PingPongRepository
import com.turnin.domain.pingPong.infrastructure.mapper.PingPongMapper.toDomain
import com.turnin.domain.pingPong.infrastructure.mapper.PingPongMapper.toPingPongDetail
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.innerJoin

class PingPongRepositoryImpl : PingPongRepository {
    override suspend fun create(
        userKeywordId: UserKeywordId,
        questionerId: UserId,
        question: PingPongContent,
    ): PingPong = suspendTransaction {
        val savedPingPongEntity = PingPongEntity.new {
            this.userKeywordId = EntityID(userKeywordId.value, UserKeywords)
            this.questionerId = EntityID(questionerId.value, Users)
            this.question = question.value
        }

        savedPingPongEntity.toDomain()
    }

    override suspend fun findVisibleById(pingPongId: PingPongId): PingPong? = suspendTransaction {
        PingPongEntity
            .find { (PingPongs.id eq pingPongId.value) and PingPongs.questionHiddenAt.isNull() }
            .singleOrNull()
            ?.toDomain()
    }

    override suspend fun findVisibleDetailsByUserKeywordId(
        currentUserId: UserId,
        userKeywordId: UserKeywordId,
        cursor: PingPongId?,
        size: Int,
        excludeBlockedQuestioners: Boolean,
    ): List<PingPongDetail> = suspendTransaction {
        // 숨김 처리된 답변은 JOIN 조건에서 제외하여, 질문만 노출되도록 한다.
        val joinQuery = PingPongs
            .innerJoin(
                otherTable = Users,
                onColumn = { PingPongs.questionerId },
                otherColumn = { Users.id },
            ).join(
                PingPongAnswers,
                JoinType.LEFT,
                PingPongs.id,
                PingPongAnswers.pingPongId,
                additionalConstraint = { PingPongAnswers.hiddenAt.isNull() },
            )

        joinQuery
            .select(
                PingPongs.id,
                PingPongs.userKeywordId,
                PingPongs.questionerId,
                PingPongs.question,
                PingPongs.createdAt,
                PingPongs.updatedAt,
                Users.id,
                Users.name,
                Users.profileImageUrl,
                PingPongAnswers.id,
                PingPongAnswers.pingPongId,
                PingPongAnswers.answer,
                PingPongAnswers.createdAt,
                PingPongAnswers.updatedAt,
            ).where {
                (PingPongs.userKeywordId eq userKeywordId.value) and PingPongs.questionHiddenAt.isNull()
            }.filterActiveUser()
            .apply {
                if (excludeBlockedQuestioners) {
                    andWhere { isNotBlockedRelationship(myUserId = currentUserId.value, PingPongs.questionerId) }
                }
            }.apply { cursor?.let { andWhere { PingPongs.id less it.value } } }
            .orderBy(PingPongs.id to SortOrder.DESC)
            .limit(size)
            .map { it.toPingPongDetail() }
    }

    override suspend fun createAnswer(
        pingPongId: PingPongId,
        answer: PingPongContent,
    ): PingPongAnswer = suspendTransaction {
        val savedPingPongAnswerEntity = PingPongAnswerEntity.new {
            this.pingPongId = EntityID(pingPongId.value, PingPongs)
            this.answer = answer.value
        }

        savedPingPongAnswerEntity.toDomain()
    }
}
