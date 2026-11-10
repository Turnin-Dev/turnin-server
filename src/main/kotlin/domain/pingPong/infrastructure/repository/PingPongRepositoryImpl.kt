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
import com.turnin.common.db.updateWithTimestamp
import com.turnin.common.model.id.PingPongAnswerId
import com.turnin.common.model.id.PingPongId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.common.util.TurninDateTime
import com.turnin.common.util.toOffsetDateTime
import com.turnin.domain.pingPong.domain.model.PingPong
import com.turnin.domain.pingPong.domain.model.PingPongAnswer
import com.turnin.domain.pingPong.domain.model.PingPongAnswerContent
import com.turnin.domain.pingPong.domain.model.PingPongAnswerCreationTarget
import com.turnin.domain.pingPong.domain.model.PingPongAnswerDeletionTarget
import com.turnin.domain.pingPong.domain.model.PingPongAnswerWithAnswerer
import com.turnin.domain.pingPong.domain.model.PingPongDetail
import com.turnin.domain.pingPong.domain.model.PingPongQuestionContent
import com.turnin.domain.pingPong.domain.repository.PingPongRepository
import com.turnin.domain.pingPong.infrastructure.mapper.PingPongMapper.toAnswer
import com.turnin.domain.pingPong.infrastructure.mapper.PingPongMapper.toDomain
import com.turnin.domain.pingPong.infrastructure.mapper.PingPongMapper.toPingPong
import com.turnin.domain.pingPong.infrastructure.mapper.PingPongMapper.toPingPongDetail
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.Case
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inSubQuery
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.booleanLiteral
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.innerJoin
import org.jetbrains.exposed.sql.or

class PingPongRepositoryImpl : PingPongRepository {
    override suspend fun createPingPong(
        userKeywordId: UserKeywordId,
        questionerId: UserId,
        question: PingPongQuestionContent,
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
        answer: PingPongAnswerContent,
    ): PingPongAnswer = suspendTransaction {
        val savedAnswerEntity = PingPongAnswerEntity.new {
            this.pingPongId = EntityID(pingPongId.value, PingPongs)
            this.answer = answer.value
        }

        savedAnswerEntity.toDomain()
    }

    override suspend fun findAnswerCreationTarget(
        answererId: UserId,
        pingPongId: PingPongId,
    ): PingPongAnswerCreationTarget? = suspendTransaction {
        // 답변자와 게시물 작성자가 차단 관계이면 게시물을 조회할 수 없는 것으로 본다. (본인이면 항상 조회 가능)
        val isOwnerVisible = Case()
            .When(
                SqlExpressionBuilder.isNotBlockedRelationship(answererId.value, UserKeywords.userId),
                booleanLiteral(true),
            ).Else(booleanLiteral(false))
        val isQuestionerBlocked = Case()
            .When(
                SqlExpressionBuilder.isNotBlockedRelationship(answererId.value, PingPongs.questionerId),
                booleanLiteral(false),
            ).Else(booleanLiteral(true))

        PingPongs
            .innerJoin(
                otherTable = UserKeywords,
                onColumn = { PingPongs.userKeywordId },
                otherColumn = { UserKeywords.id },
            ).innerJoin(
                otherTable = Users,
                onColumn = { UserKeywords.userId },
                otherColumn = { Users.id },
            ).select(
                PingPongs.id,
                PingPongs.userKeywordId,
                PingPongs.questionerId,
                PingPongs.question,
                PingPongs.createdAt,
                PingPongs.updatedAt,
                UserKeywords.userId,
                UserKeywords.isActive,
                Users.isActive,
                isOwnerVisible,
                isQuestionerBlocked,
            ).where {
                (PingPongs.id eq pingPongId.value) and PingPongs.questionHiddenAt.isNull()
            }.singleOrNull()
            ?.let { row ->
                // 게시물/작성자가 비활성이거나 요청자와 작성자가 차단 관계이면 작성자 ID를 비운다.
                // (이 조건을 WHERE에 넣으면 '핑퐁 없음'과 구분할 수 없어 조회 후 판단한다)
                val isOwnerAvailable = row[UserKeywords.isActive] && row[Users.isActive] && row[isOwnerVisible]
                PingPongAnswerCreationTarget(
                    pingPong = row.toPingPong(),
                    ownerId = if (isOwnerAvailable) UserId(row[UserKeywords.userId].value) else null,
                    isQuestionerBlocked = row[isQuestionerBlocked],
                )
            }
    }

    override suspend fun findAnswerDeletionTarget(
        pingPongId: PingPongId,
    ): PingPongAnswerDeletionTarget? = suspendTransaction {
        // 답변은 LEFT JOIN으로 붙여 '핑퐁 없음'과 '답변 없음'을 구분하고,
        // 숨김 처리된 답변은 JOIN 조건에서 제외해 '답변 없음'으로 취급한다. (질문당 답변은 최대 1개)
        PingPongs
            .innerJoin(
                otherTable = UserKeywords,
                onColumn = { PingPongs.userKeywordId },
                otherColumn = { UserKeywords.id },
            ).innerJoin(
                otherTable = Users,
                onColumn = { UserKeywords.userId },
                otherColumn = { Users.id },
            ).join(
                PingPongAnswers,
                JoinType.LEFT,
                PingPongs.id,
                PingPongAnswers.pingPongId,
                additionalConstraint = { PingPongAnswers.hiddenAt.isNull() },
            ).select(
                UserKeywords.userId,
                UserKeywords.isActive,
                Users.isActive,
                PingPongAnswers.id,
            ).where {
                (PingPongs.id eq pingPongId.value) and PingPongs.questionHiddenAt.isNull()
            }.singleOrNull()
            ?.let { row ->
                // 게시물/작성자가 비활성이면 작성자 ID를 비운다.
                // (이 조건을 WHERE에 넣으면 '핑퐁 없음'과 구분할 수 없어 조회 후 판단한다)
                val isOwnerAvailable = row[UserKeywords.isActive] && row[Users.isActive]
                PingPongAnswerDeletionTarget(
                    ownerId = if (isOwnerAvailable) UserId(row[UserKeywords.userId].value) else null,
                    answerId = row.getOrNull(PingPongAnswers.id)?.let { PingPongAnswerId(it.value) },
                )
            }
    }

    override suspend fun findVisibleAnswerWithAnswererById(
        answerId: PingPongAnswerId,
    ): PingPongAnswerWithAnswerer? = suspendTransaction {
        // 답변자 = 질문이 달린 게시물(사용자 키워드)의 작성자
        PingPongAnswers
            .innerJoin(
                otherTable = PingPongs,
                onColumn = { PingPongAnswers.pingPongId },
                otherColumn = { PingPongs.id },
            ).innerJoin(
                otherTable = UserKeywords,
                onColumn = { PingPongs.userKeywordId },
                otherColumn = { UserKeywords.id },
            ).select(
                PingPongAnswers.id,
                PingPongAnswers.pingPongId,
                PingPongAnswers.answer,
                PingPongAnswers.createdAt,
                PingPongAnswers.updatedAt,
                UserKeywords.userId,
            ).where {
                (PingPongAnswers.id eq answerId.value) and
                    PingPongAnswers.hiddenAt.isNull() and
                    PingPongs.questionHiddenAt.isNull()
            }.singleOrNull()
            ?.let { row ->
                PingPongAnswerWithAnswerer(
                    answer = row.toAnswer(),
                    answererId = UserId(row[UserKeywords.userId].value),
                )
            }
    }

    override suspend fun deletePingPong(pingPongId: PingPongId): Boolean = suspendTransaction {
        PingPongs.deleteWhere { PingPongs.id eq pingPongId.value } > 0
    }

    override suspend fun deleteAnswer(answerId: PingPongAnswerId): Boolean = suspendTransaction {
        PingPongAnswers.deleteWhere { PingPongAnswers.id eq answerId.value } > 0
    }

    override suspend fun hideQuestion(pingPongId: PingPongId): Boolean = suspendTransaction {
        PingPongs.updateWithTimestamp({ (PingPongs.id eq pingPongId.value) and PingPongs.questionHiddenAt.isNull() }) {
            it[questionHiddenAt] = TurninDateTime.now().toOffsetDateTime()
        } > 0
    }

    override suspend fun hideAnswer(answerId: PingPongAnswerId): Boolean = suspendTransaction {
        PingPongAnswers.updateWithTimestamp({
            (PingPongAnswers.id eq answerId.value) and PingPongAnswers.hiddenAt.isNull()
        }) {
            it[hiddenAt] = TurninDateTime.now().toOffsetDateTime()
        } > 0
    }

    override suspend fun deleteAllByUserId(userId: UserId): Unit = suspendTransaction {
        // 질문자 조건(questioner_id)은 인덱스가 없어 seq scan 된다. (PingPongs 주석 참고)
        val userKeywordIdsOfUser = UserKeywords
            .select(UserKeywords.id)
            .where { UserKeywords.userId eq userId.value }

        PingPongs.deleteWhere {
            (questionerId eq userId.value) or (userKeywordId inSubQuery userKeywordIdsOfUser)
        }
    }

    override suspend fun deleteAllByUserKeywordId(userKeywordId: UserKeywordId): Unit = suspendTransaction {
        PingPongs.deleteWhere { PingPongs.userKeywordId eq userKeywordId.value }
    }
}
