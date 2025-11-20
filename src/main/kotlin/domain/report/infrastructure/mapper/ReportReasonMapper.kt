package com.peekr.domain.report.infrastructure.mapper

import com.peekr.common.db.schema.ReportReasonEntity
import com.peekr.common.db.schema.ReportReasons
import com.peekr.common.model.ReportReasonId
import com.peekr.domain.report.domain.model.ReportReason
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
