package com.peekr.common.db.scheme

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID

object Reports : BaseLongIdTable("report") {
    val reporterId = reference("reporter_id", Users)
    val reportedId = reference("reported_id", Users)
    val reasonId = reference("reason_id", ReportReasons)
    val customReason = text("custom_reason").nullable()

    init {
        index("idx_report_pair", false, reporterId, reportedId)
    }
}

class ReportEntity(id: EntityID<Long>) : BaseEntity(id, Reports) {
    companion object : BaseEntityClass<ReportEntity>(Reports)

    var reporterId by Reports.reporterId
    var reportedId by Reports.reportedId
    var reasonId by Reports.reasonId
    var customReason by Reports.customReason
}
