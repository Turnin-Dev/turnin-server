package com.peekr.common.db.schema

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ReferenceOption

object Reports : BaseLongIdTable("report") {
    val reporterId = reference("reporter_id", Users, onDelete = ReferenceOption.CASCADE)
    val reportedId = reference("reported_id", Users, onDelete = ReferenceOption.CASCADE)
    val reasonId = reference("reason_id", ReportReasons)
    val customReason = text("custom_reason").nullable()

    init {
        // 동일 신고자→피신고자에 대해 동일 사유(reason) 중복 신고를 막으려면 유니크 인덱스(3컬럼) 생성
        uniqueIndex("uq_report_reporter_reported_reason", reporterId, reportedId, reasonId)
    }
}

class ReportEntity(id: EntityID<Long>) : BaseEntity(id, Reports) {
    companion object : BaseEntityClass<ReportEntity>(Reports)

    var reporterId by Reports.reporterId
    var reportedId by Reports.reportedId
    var reasonId by Reports.reasonId
    var customReason by Reports.customReason
}
