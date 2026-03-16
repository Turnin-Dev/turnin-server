package com.peekr.common.db.schema

import com.peekr.common.db.BaseEntityClassWithoutTimestamp
import com.peekr.common.db.BaseEntityWithoutTimestamp
import com.peekr.common.db.BaseLongIdTableWithoutTimestamp
import org.jetbrains.exposed.dao.id.EntityID

/** 신고 사유 엔티티 클래스 (Exposed DSL 방식) */
object ReportReasons : BaseLongIdTableWithoutTimestamp("report_reason") {
    val code = varchar("code", 50).uniqueIndex()
    val description = text("description")
}

/** 신고 사유 엔티티 클래스 (Exposed DAO/ORM 방식) */
class ReportReasonEntity(id: EntityID<Long>) : BaseEntityWithoutTimestamp(id) {
    companion object : BaseEntityClassWithoutTimestamp<ReportReasonEntity>(ReportReasons)

    var code by ReportReasons.code
    var description by ReportReasons.description
}
