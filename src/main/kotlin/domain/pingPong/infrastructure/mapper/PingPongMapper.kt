package com.turnin.domain.pingPong.infrastructure.mapper

import com.turnin.common.db.schema.PingPongAnswerEntity
import com.turnin.common.db.schema.PingPongEntity
import com.turnin.common.model.id.PingPongAnswerId
import com.turnin.common.model.id.PingPongId
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.pingPong.domain.model.PingPong
import com.turnin.domain.pingPong.domain.model.PingPongAnswer
import com.turnin.domain.pingPong.domain.model.PingPongContent

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
}
