package com.peekr.common.db.schema

import com.peekr.common.db.BaseEntityWithoutTimestamp
import com.peekr.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID

/** 신고 사유 엔티티 클래스 (복수형) */
object ReportReasons : BaseLongIdTable("report_reason") {
    val code = varchar("code", 50).uniqueIndex()
    val description = text("description")
}

/** 신고 사유 엔티티 클래스 (단수형) */
class ReportReasonEntity(id: EntityID<Long>) : BaseEntityWithoutTimestamp(id) {
    var code by ReportReasons.code
    var description by ReportReasons.description
}
