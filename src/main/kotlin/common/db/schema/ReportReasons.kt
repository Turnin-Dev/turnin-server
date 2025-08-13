package com.peekr.common.db.schema

import com.peekr.common.db.BaseEntityWithoutTimestamp
import com.peekr.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID

object ReportReasons : BaseLongIdTable("report_reason") {
    val code = varchar("code", 50).uniqueIndex()
    val description = text("description")
}

class ReportReasonEntity(id: EntityID<Long>) : BaseEntityWithoutTimestamp(id) {
    var code by ReportReasons.code
    var description by ReportReasons.description
}
