package com.peekr.common.db.scheme

import com.peekr.common.db.BaseEntity
import com.peekr.common.db.BaseEntityClass
import com.peekr.common.db.BaseLongIdTable
import org.jetbrains.exposed.dao.id.EntityID

object ReportReasons : BaseLongIdTable("report_reason") {
    val code = varchar("code", 50).uniqueIndex()
    val description = text("description")
}

class ReportReasonEntity(id: EntityID<Long>) : BaseEntity(id, ReportReasons) {
    companion object : BaseEntityClass<ReportReasonEntity>(ReportReasons)

    var code by ReportReasons.code
    var description by ReportReasons.description
}
