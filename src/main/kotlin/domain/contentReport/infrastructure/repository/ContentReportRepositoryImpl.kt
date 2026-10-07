package com.turnin.domain.contentReport.infrastructure.repository

import com.turnin.common.db.schema.ContentReports
import com.turnin.common.db.suspendTransaction
import com.turnin.common.model.ContentReportType
import com.turnin.domain.contentReport.domain.model.ContentReportDetail
import com.turnin.domain.contentReport.domain.repository.ContentReportRepository
import org.jetbrains.exposed.sql.VarCharColumnType
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll

class ContentReportRepositoryImpl : ContentReportRepository {
    override suspend fun lockContent(
        contentType: ContentReportType,
        contentId: Long,
    ): Unit = suspendTransaction {
        // PostgreSQL 트랜잭션 수준 advisory lock (커밋/롤백 시 자동 해제)
        // 다른 기능의 advisory lock과 키가 겹치지 않도록 기능 접두사를 포함한 문자열을 64비트 키로 해시한다.
        // (해시 충돌 시에도 서로 다른 콘텐츠가 함께 직렬화될 뿐 정합성에는 영향이 없다)
        exec(
            stmt = "SELECT pg_advisory_xact_lock(hashtextextended(?, 0))",
            args = listOf(VarCharColumnType() to "content_report:${contentType.name}:$contentId"),
        ) { }
    }

    override suspend fun create(contentReportDetail: ContentReportDetail): Unit = suspendTransaction {
        // DAO(new)는 INSERT가 flush 시점까지 지연되므로, 제약 조건 위반을 이 시점에 감지하도록 DSL로 즉시 INSERT 한다.
        ContentReports.insert {
            it[reporterId] = contentReportDetail.reporterId.value
            it[reportedUserId] = contentReportDetail.reportedUserId.value
            it[contentType] = contentReportDetail.contentType
            it[contentId] = contentReportDetail.contentId
            it[contentSnapshot] = contentReportDetail.contentSnapshot
            it[reasonId] = contentReportDetail.reasonId.value
            it[customReason] = contentReportDetail.customReason
        }
    }

    override suspend fun countByContent(
        contentType: ContentReportType,
        contentId: Long,
    ): Long = suspendTransaction {
        // 유니크 인덱스(content_type, content_id, reporter_id)의 선행 컬럼으로 집계한다.
        // (eqEnum은 컬럼을 문자열로 캐스팅하여 인덱스를 타지 못하므로 enum 타입 그대로 비교한다)
        ContentReports
            .selectAll()
            .where { (ContentReports.contentType eq contentType) and (ContentReports.contentId eq contentId) }
            .count()
    }
}
