package com.turnin.common.db.schema

import com.turnin.common.db.BaseEntity
import com.turnin.common.db.BaseEntityClass
import com.turnin.common.db.BaseLongIdTable
import com.turnin.common.db.DatabaseUtils.timestamptz
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption

/** 핑퐁 엔티티 클래스 (Exposed DSL 방식) */
object PingPongs : BaseLongIdTable("ping_pong") {
    const val MAX_CONTENT_LENGTH = 2200

    val userKeywordId = reference("user_keyword_id", UserKeywords, onDelete = ReferenceOption.CASCADE)
    val questionerId = reference("questioner_id", Users, onDelete = ReferenceOption.CASCADE)
    val question = varchar("question", MAX_CONTENT_LENGTH)

    /** 신고 누적으로 숨김 처리된 시각 */
    val questionHiddenAt = timestamptz("question_hidden_at", setDefault = false).nullable()

    init {
        // 게시물별 핑퐁 목록 조회 (id 역순 = 최신순)
        index("idx_ping_pong_user_keyword_id", false, userKeywordId, id)

        // questionerId 인덱스는 저장 용량 절약을 위해 두지 않는다.
        // 계정 삭제(탈퇴) 시 질문자 조건으로 ping_pong을 seq scan 하므로, 탈퇴 API가 느려지면 새 마이그레이션으로 인덱스를 추가한다.
        // (연속 작성 제한은 DB가 아닌 RateLimit 플러그인으로 처리)
    }
}

/** 핑퐁 엔티티 클래스 (Exposed DAO/ORM 방식) */
class PingPongEntity(id: EntityID<Long>) : BaseEntity(id, PingPongs) {
    companion object : BaseEntityClass<PingPongEntity>(PingPongs)

    var userKeywordId by PingPongs.userKeywordId
    var questionerId by PingPongs.questionerId
    var question by PingPongs.question
    var questionHiddenAt by PingPongs.questionHiddenAt
}
