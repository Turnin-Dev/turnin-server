package com.turnin.common.db.schema

import com.turnin.common.db.BaseEntity
import com.turnin.common.db.BaseEntityClass
import com.turnin.common.db.BaseLongIdTable
import com.turnin.common.db.DatabaseUtils.customPostgresEnum
import com.turnin.common.model.ContentReportType
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption

/**
 * 콘텐츠 신고 엔티티 클래스 (Exposed DSL 방식)
 *
 * 신고 시점의 콘텐츠 내용을 스냅샷으로 함께 보관하며, 원본 콘텐츠가 삭제되어도 유지된다.
 * [contentId]는 [contentType]에 따라 참조 대상 테이블이 달라지므로 FK를 두지 않는다.
 * (질문: `ping_pong.id`, 답변: `ping_pong_answer.id`)
 */
object ContentReports : BaseLongIdTable("content_report") {
    val reporterId = reference("reporter_id", Users, onDelete = ReferenceOption.CASCADE)
    val reportedUserId = reference("reported_user_id", Users, onDelete = ReferenceOption.CASCADE)
    val contentType = customPostgresEnum<ContentReportType>("content_type", "content_report_type")
    val contentId = long("content_id")
    val contentSnapshot = text("content_snapshot")
    val reasonId = reference("reason_id", ReportReasons, onDelete = ReferenceOption.RESTRICT)
    val customReason = text("custom_reason").nullable()

    init {
        // 한 사람이 한 콘텐츠에 대해 한 번만 신고 가능 (선행 컬럼으로 콘텐츠별 신고 수 집계에도 사용)
        uniqueIndex("uq_content_report_content_reporter", contentType, contentId, reporterId)

        // 신고 발생 빈도가 낮아 테이블이 작으므로 reporterId / reportedUserId 인덱스는 두지 않는다.
        // 계정 Hard Delete 시 CASCADE가 seq scan 하므로, 배치가 느려지면 인덱스를 추가한다.
        // 자기 자신의 콘텐츠 신고 방지
        check("chk_content_report_not_self") { reporterId neq reportedUserId }
    }
}

/** 콘텐츠 신고 엔티티 클래스 (Exposed DAO/ORM 방식) */
class ContentReportEntity(id: EntityID<Long>) : BaseEntity(id, ContentReports) {
    companion object : BaseEntityClass<ContentReportEntity>(ContentReports)

    var reporterId by ContentReports.reporterId
    var reportedUserId by ContentReports.reportedUserId
    var contentType by ContentReports.contentType
    var contentId by ContentReports.contentId
    var contentSnapshot by ContentReports.contentSnapshot
    var reasonId by ContentReports.reasonId
    var customReason by ContentReports.customReason
}
