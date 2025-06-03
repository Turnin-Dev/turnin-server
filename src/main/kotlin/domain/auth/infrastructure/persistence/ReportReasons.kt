package com.peekr.domain.auth.infrastructure.persistence

import org.jetbrains.exposed.dao.id.LongIdTable

object ReportReasons : LongIdTable("report_reason") {
    val code = varchar("code", 50).uniqueIndex()
    val description = text("description")
}
