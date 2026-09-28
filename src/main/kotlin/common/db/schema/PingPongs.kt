package com.turnin.common.db.schema

import com.turnin.common.db.BaseEntity
import com.turnin.common.db.BaseEntityClass
import com.turnin.common.db.BaseLongIdTable
import com.turnin.common.db.DatabaseUtils.customPostgresEnum
import com.turnin.common.db.DatabaseUtils.timestamptz
import com.turnin.common.model.PingPongStatus
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.or

/** 핑퐁 엔티티 클래스 (Exposed DSL 방식) */
object PingPongs : BaseLongIdTable("ping_pong") {
    const val MAX_CONTENT_LENGTH = 2200

    val userKeywordId = reference("user_keyword_id", UserKeywords, onDelete = ReferenceOption.CASCADE)
    val questionerId = reference("questioner_id", Users, onDelete = ReferenceOption.CASCADE)
    val question = varchar("question", MAX_CONTENT_LENGTH)
    val answer = varchar("answer", MAX_CONTENT_LENGTH).nullable()
    val status = customPostgresEnum<PingPongStatus>("status", "ping_pong_status").default(PingPongStatus.PENDING)
    val answeredAt = timestamptz("answered_at", setDefault = false).nullable()

    init {
        // 게시물별 핑퐁 목록 조회 (최신순)
        index("idx_ping_pong_user_keyword_created", false, userKeywordId, createdAt, id)
        // 동일 사용자의 동일 게시물 연속 작성 제한 확인
        index("idx_ping_pong_questioner_keyword_created", false, questionerId, userKeywordId, createdAt)
        // 답변 내용과 답변 시각은 항상 함께 존재하거나 함께 없어야 한다.
        check("chk_ping_pong_answer_consistency") {
            (answer.isNull() and answeredAt.isNull()) or (answer.isNotNull() and answeredAt.isNotNull())
        }
    }
}

/** 핑퐁 엔티티 클래스 (Exposed DAO/ORM 방식) */
class PingPongEntity(id: EntityID<Long>) : BaseEntity(id, PingPongs) {
    companion object : BaseEntityClass<PingPongEntity>(PingPongs)

    var userKeywordId by PingPongs.userKeywordId
    var questionerId by PingPongs.questionerId
    var question by PingPongs.question
    var answer by PingPongs.answer
    var status by PingPongs.status
    var answeredAt by PingPongs.answeredAt
}
