package com.turnin.domain.pingPong.infrastructure.repository

import com.turnin.common.db.schema.PingPongAnswerEntity
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
import com.turnin.domain.pingPong.domain.repository.PingPongRepository
import com.turnin.domain.pingPong.infrastructure.mapper.PingPongMapper.toDomain
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.and

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
