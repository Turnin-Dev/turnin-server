package com.peekr.domain.report.infrastructure.repository

import com.peekr.common.db.schema.ReportEntity
import com.peekr.common.db.schema.ReportReasonEntity
import com.peekr.common.db.schema.ReportReasons
import com.peekr.common.db.schema.Reports
import com.peekr.common.db.schema.UserKeywords
import com.peekr.common.db.schema.Users
import com.peekr.common.db.suspendTransaction
import com.peekr.common.model.id.UserKeywordId
import com.peekr.domain.report.domain.model.ReportDetail
import com.peekr.domain.report.domain.model.ReportReason
import com.peekr.domain.report.domain.repository.ReportRepository
import com.peekr.domain.report.infrastructure.mapper.ReportReasonMapper.toDomain
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.selectAll

class ReportRepositoryImpl : ReportRepository {
    override suspend fun existsByUserKeywordId(userKeywordId: UserKeywordId): Boolean = suspendTransaction {
        Reports
            .selectAll()
            .where { Reports.reportedUserKeywordId eq userKeywordId.value }
            .count() > 0
    }

    override suspend fun getReportReasons(): List<ReportReason> = suspendTransaction {
        ReportReasons
            .selectAll()
            .map { it.toDomain() }
    }

    override suspend fun createReportReason(
        code: String,
        description: String,
    ): ReportReason = suspendTransaction {
        ReportReasonEntity
            .new {
                this.code = code
                this.description = description
            }.toDomain()
    }

    override suspend fun createReport(
        reportDetail: ReportDetail,
    ): Unit = suspendTransaction {
        ReportEntity.new {
            this.reporterId = EntityID(reportDetail.reporterId.value, Users)
            this.reportedId = reportDetail.reportedId?.let {
                EntityID(it.value, Users)
            }
            this.reportedUserKeywordId = reportDetail.reportedUserKeywordId?.let {
                EntityID(it.value, UserKeywords)
            }
            this.reasonId = EntityID(reportDetail.reasonId.value, ReportReasons)
            this.customReason = reportDetail.customReason
        }
    }
}
