package com.peekr.common.db.schema

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption

/** 신고 엔티티 클래스 (복수형) */
object Reports : BaseLongIdTable("report") {
    val reporterId = reference("reporter_id", Users, onDelete = ReferenceOption.RESTRICT)
    val reportedId = reference("reported_id", Users, onDelete = ReferenceOption.RESTRICT).nullable()
    val reportedUserKeywordId = reference(
        "reported_user_keyword_id",
        UserKeywords,
        onDelete = ReferenceOption.SET_NULL,
    ).nullable()
    val reasonId = reference("reason_id", ReportReasons, onDelete = ReferenceOption.RESTRICT)
    val customReason = text("custom_reason").nullable()

    init {
        // 한 사람이 한 대상에 대해 한 번만 신고 가능하도록 제약
        uniqueIndex("uq_report_reporter_user", reporterId, reportedId)
        uniqueIndex("uq_report_reporter_keyword", reporterId, reportedUserKeywordId)
    }
}

/** 신고 엔티티 클래스 (단수형) */
class ReportEntity(id: EntityID<Long>) : BaseEntity(id, Reports) {
    companion object : BaseEntityClass<ReportEntity>(Reports)

    var reporterId by Reports.reporterId
    var reportedId by Reports.reportedId
    var reportedUserKeywordId by Reports.reportedUserKeywordId
    var reasonId by Reports.reasonId
    var customReason by Reports.customReason
}
