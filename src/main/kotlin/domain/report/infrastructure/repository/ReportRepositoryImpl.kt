package com.peekr.domain.report.infrastructure.repository

import com.peekr.common.db.schema.ReportEntity
import com.peekr.common.db.schema.ReportReasonEntity
import com.peekr.common.db.schema.ReportReasons
import com.peekr.common.db.schema.Users
import com.peekr.common.db.suspendTransaction
import com.peekr.domain.report.domain.model.Report
import com.peekr.domain.report.domain.model.ReportReason
import com.peekr.domain.report.domain.repository.ReportRepository
import com.peekr.domain.report.infrastructure.mapper.ReportReasonMapper.toDomain
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.selectAll

class ReportRepositoryImpl : ReportRepository {
    override suspend fun getReportReasons(): List<ReportReason> = suspendTransaction {
        ReportReasons
            .selectAll()
            .map { it.toDomain() }
    }

    override suspend fun createReportReason(
        code: String,
        description: String,
    ): ReportReason? = suspendTransaction {
        ReportReasonEntity
            .new {
                this.code = code
                this.description = description
            }.toDomain()
    }

    override suspend fun createReport(
        report: Report,
    ): Unit = suspendTransaction {
        ReportEntity.new {
            this.reporterId = EntityID(report.reporterId.value, Users)
            this.reportedId = EntityID(report.reportedId.value, Users)
            this.reasonId = EntityID(report.reasonId.value, ReportReasons)
            this.customReason = report.customReason
        }
    }
}
