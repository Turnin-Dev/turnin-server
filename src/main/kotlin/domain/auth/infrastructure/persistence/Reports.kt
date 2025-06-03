package com.peekr.domain.auth.infrastructure.persistence

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.CurrentTimestamp
import org.jetbrains.exposed.sql.javatime.timestamp

object Reports : LongIdTable("report") {
    val reporterId = reference("reporter_id", Users)
    val reportedId = reference("reported_id", Users)
    val reasonId = reference("reason_id", ReportReasons)
    val customReason = text("custom_reason").nullable()
    val createdAt = timestamp("created_at").defaultExpression(CurrentTimestamp)

    init {
        index("idx_report_pair", false, reporterId, reportedId)
    }
}
