package com.turnin.domain.report.infrastructure.mapper

import com.turnin.common.db.schema.ReportReasonEntity
import com.turnin.common.db.schema.ReportReasons
import com.turnin.common.model.id.ReportReasonId
import com.turnin.domain.report.domain.model.ReportReason
import org.jetbrains.exposed.sql.ResultRow

object ReportReasonMapper {
    fun ResultRow.toDomain(): ReportReason =
        ReportReason(
            id = ReportReasonId(this[ReportReasons.id].value),
            code = this[ReportReasons.code],
            description = this[ReportReasons.description],
        )

    fun ReportReasonEntity.toDomain(): ReportReason =
        ReportReason(
            id = ReportReasonId(this.id.value),
            code = this.code,
            description = this.description,
        )
}
