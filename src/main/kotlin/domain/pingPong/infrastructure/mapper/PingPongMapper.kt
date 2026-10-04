package com.turnin.domain.pingPong.infrastructure.mapper

import com.turnin.common.db.schema.PingPongAnswerEntity
import com.turnin.common.db.schema.PingPongAnswers
import com.turnin.common.db.schema.PingPongEntity
import com.turnin.common.db.schema.PingPongs
import com.turnin.common.db.schema.Users
import com.turnin.common.model.UserName
import com.turnin.common.model.id.PingPongAnswerId
import com.turnin.common.model.id.PingPongId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.pingPong.domain.model.PingPong
import com.turnin.domain.pingPong.domain.model.PingPongAnswer
import com.turnin.domain.pingPong.domain.model.PingPongContent
import com.turnin.domain.pingPong.domain.model.PingPongDetail
import com.turnin.domain.pingPong.domain.model.PingPongQuestioner
import org.jetbrains.exposed.sql.ResultRow

internal object PingPongMapper {
    fun PingPongEntity.toDomain(): PingPong =
        PingPong(
            id = PingPongId(this.id.value),
            userKeywordId = UserKeywordId(this.userKeywordId.value),
            questionerId = UserId(this.questionerId.value),
            question = PingPongContent(this.question),
            createdAt = this.createdAt.toEpochSecond(),
            updatedAt = this.updatedAt.toEpochSecond(),
        )

    fun PingPongAnswerEntity.toDomain(): PingPongAnswer =
        PingPongAnswer(
            id = PingPongAnswerId(this.id.value),
            pingPongId = PingPongId(this.pingPongId.value),
            answer = PingPongContent(this.answer),
            createdAt = this.createdAt.toEpochSecond(),
            updatedAt = this.updatedAt.toEpochSecond(),
        )

    /**
     * `ping_pong` + `user`(질문자) + `ping_pong_answer`(LEFT JOIN) 조회 결과를 [PingPongDetail]로 변환한다.
     */
    fun ResultRow.toPingPongDetail(): PingPongDetail =
        PingPongDetail(
            pingPong = PingPong(
                id = PingPongId(this[PingPongs.id].value),
                userKeywordId = UserKeywordId(this[PingPongs.userKeywordId].value),
                questionerId = UserId(this[PingPongs.questionerId].value),
                question = PingPongContent(this[PingPongs.question]),
                createdAt = this[PingPongs.createdAt].toEpochSecond(),
                updatedAt = this[PingPongs.updatedAt].toEpochSecond(),
            ),
            questioner = PingPongQuestioner(
                userId = UserId(this[Users.id].value),
                userName = UserName(this[Users.name]),
                profileImageUrl = this[Users.profileImageUrl],
            ),
            answer = this.getOrNull(PingPongAnswers.id)?.let { this.toPingPongAnswer() },
        )

    /**
     * `ping_pong_answer` 컬럼을 포함한 조회 결과를 [PingPongAnswer]로 변환한다.
     */
    fun ResultRow.toPingPongAnswer(): PingPongAnswer =
        PingPongAnswer(
            id = PingPongAnswerId(this[PingPongAnswers.id].value),
            pingPongId = PingPongId(this[PingPongAnswers.pingPongId].value),
            answer = PingPongContent(this[PingPongAnswers.answer]),
            createdAt = this[PingPongAnswers.createdAt].toEpochSecond(),
            updatedAt = this[PingPongAnswers.updatedAt].toEpochSecond(),
        )
}
