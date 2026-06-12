package com.turnin.domain.report.infrastructure.repository

import com.turnin.common.db.schema.ReportEntity
import com.turnin.common.db.schema.ReportReasonEntity
import com.turnin.common.db.schema.ReportReasons
import com.turnin.common.db.schema.Reports
import com.turnin.common.db.schema.UserKeywords
import com.turnin.common.db.schema.Users
import com.turnin.common.db.suspendTransaction
import com.turnin.common.model.id.UserId
import com.turnin.common.model.id.UserKeywordId
import com.turnin.domain.report.domain.model.ReportDetail
import com.turnin.domain.report.domain.model.ReportReason
import com.turnin.domain.report.domain.repository.ReportRepository
import com.turnin.domain.report.infrastructure.mapper.ReportReasonMapper.toDomain
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll

class ReportRepositoryImpl : ReportRepository {
    override suspend fun existsByUserKeywordId(userKeywordId: UserKeywordId): Boolean = suspendTransaction {
        Reports
            .selectAll()
            .where { Reports.reportedUserKeywordId eq userKeywordId.value }
            .limit(1)
            .empty()
            .not()
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

    override suspend fun deleteByUserId(userId: UserId): Unit = suspendTransaction {
        Reports.deleteWhere {
            (Reports.reporterId eq userId.value) or
                (Reports.reportedId eq userId.value)
        }
    }
}
