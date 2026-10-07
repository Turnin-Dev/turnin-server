package com.turnin.common.db.schema

import com.turnin.common.db.BaseEntity
import com.turnin.common.db.BaseEntityClass
import com.turnin.common.db.BaseLongIdTable
import com.turnin.common.db.DatabaseUtils.timestamptz
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption

/**
 * 핑퐁 답변 엔티티 클래스 (Exposed DSL 방식)
 *
 * 질문당 최대 1개이며, 답변 삭제 후 재답변 시 새 id가 부여되어 답변 단위로 신고가 구분된다.
 */
object PingPongAnswers : BaseLongIdTable("ping_pong_answer") {
    val pingPongId = reference("ping_pong_id", PingPongs, onDelete = ReferenceOption.CASCADE)
        .uniqueIndex("uq_ping_pong_answer_ping_pong")
    val answer = varchar("answer", PingPongs.MAX_CONTENT_LENGTH)

    /** 신고 누적으로 숨김 처리된 시각 */
    val hiddenAt = timestamptz("hidden_at", setDefault = false).nullable()
}

/** 핑퐁 답변 엔티티 클래스 (Exposed DAO/ORM 방식) */
class PingPongAnswerEntity(id: EntityID<Long>) : BaseEntity(id, PingPongAnswers) {
    companion object : BaseEntityClass<PingPongAnswerEntity>(PingPongAnswers)

    var pingPongId by PingPongAnswers.pingPongId
    var answer by PingPongAnswers.answer
    var hiddenAt by PingPongAnswers.hiddenAt
}
